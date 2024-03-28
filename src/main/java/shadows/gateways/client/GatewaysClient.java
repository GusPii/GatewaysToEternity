package shadows.gateways.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.gateways.GatewayObjects;
import shadows.gateways.Gateways;
import shadows.gateways.entity.GatewayEntity;
import shadows.gateways.gate.Failure;
import shadows.gateways.gate.Gateway;
import shadows.gateways.gate.Reward;
import shadows.gateways.gate.WaveEntity;
import shadows.gateways.item.GatePearlItem;
import shadows.placebo.PlaceboClient;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.util.AttributeHelper;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT, modid = Gateways.MODID)
public class GatewaysClient {

	@SubscribeEvent
	public static void setup(FMLClientSetupEvent e) {
		e.enqueueWork(() -> {
			ItemProperties.register(GatewayObjects.GATE_PEARL.get(), new ResourceLocation(Gateways.MODID, "size"), (stack, level, entity, seed) -> {
				Gateway gate = GatePearlItem.getGate(stack);
				if (gate == null) return 2;
				return gate.getSize().ordinal();
			});
		});
		MinecraftForge.EVENT_BUS.addListener(GatewaysClient::bossRenderPre);
		MinecraftForge.EVENT_BUS.addListener(GatewaysClient::tooltip);
		MinecraftForge.EVENT_BUS.addListener(GatewaysClient::scroll);
		MinecraftForge.EVENT_BUS.addListener(GatewaysClient::scroll2);
	}

	@SubscribeEvent
	public static void colors(RegisterColorHandlersEvent.Item e) {
		e.register((stack, tint) -> {
			Gateway gate = GatePearlItem.getGate(stack);
			if (gate != null) return gate.getColor().getValue();
			return 0xAAAAFF;
		}, GatewayObjects.GATE_PEARL.get());
	}

	@SubscribeEvent
	public static void eRenders(RegisterRenderers e) {
		e.registerEntityRenderer(GatewayObjects.GATEWAY.get(), GatewayRenderer::new);
	}

	@SubscribeEvent
	public static void factories(RegisterParticleProvidersEvent e) {
		e.register(GatewayObjects.GLOW.get(), GatewayParticle.Factory::new);
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public static void stitch(TextureStitchEvent.Pre e) {
		if (e.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			e.addSprite(new ResourceLocation(Gateways.MODID, "particle/glow"));
		}
	}

	private static int waveIdx = 0;
	private static ItemStack currentTooltipItem = ItemStack.EMPTY;
	private static long tooltipTick = 0;

	public static void scroll(ScreenEvent.MouseScrolled.Pre e) {
		if (currentTooltipItem.getItem() == GatewayObjects.GATE_PEARL.get() && tooltipTick == PlaceboClient.ticks && Screen.hasShiftDown()) {
			waveIdx += e.getScrollDelta() < 0 ? 1 : -1;
			e.setCanceled(true);
		} else {
			waveIdx = 0;
		}
	}

	public static void scroll2(InputEvent.MouseScrollingEvent e) {
		if (currentTooltipItem.getItem() == GatewayObjects.GATE_PEARL.get() && tooltipTick == PlaceboClient.ticks && Screen.hasShiftDown()) {
			waveIdx += e.getScrollDelta() < 0 ? 1 : -1;
			e.setCanceled(true);
		} else {
			waveIdx = 0;
		}
	}

	static RandomSource rand = RandomSource.create();

	public static void tooltip(ItemTooltipEvent e) {
		currentTooltipItem = e.getItemStack();
		tooltipTick = PlaceboClient.ticks;
		if (e.getItemStack().getItem() == GatewayObjects.GATE_PEARL.get()) {
			Gateway gate = GatePearlItem.getGate(e.getItemStack());
			List<Component> tooltips = e.getToolTip();
			if (gate == null) {
				tooltips.add(Component.literal("Errored Gate Pearl, file a bug report detailing how you obtained this."));
				return;
			}

			Component comp;

			if (Screen.hasShiftDown()) {
				waveIdx = Math.floorMod(waveIdx, gate.getNumWaves());

				int wave = waveIdx;
				Component sub = Component.translatable("tooltip.gateways.scroll").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true).withUnderlined(false));
				comp = Component.translatable("tooltip.gateways.wave", wave + 1, gate.getNumWaves(), sub).withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE);
				tooltips.add(comp);
				tooltips.add(Component.nullToEmpty(null));
				comp = Component.translatable("tooltip.gateways.entities").withStyle(ChatFormatting.BLUE);
				tooltips.add(comp);
				Map<String, Integer> counts = new HashMap<>();
				for (WaveEntity entity : gate.getWave(wave).entities()) {
					counts.put(entity.getDescription().getString(), counts.getOrDefault(entity.getDescription().getString(), 0) + 1);
				}
				for (Map.Entry<String, Integer> counted : counts.entrySet()) {
					comp = Component.translatable("tooltip.gateways.list1", counted.getValue(), Component.translatable(counted.getKey())).withStyle(ChatFormatting.BLUE);
					tooltips.add(comp);
				}
				if (!gate.getWave(wave).modifiers().isEmpty()) {
					comp = Component.translatable("tooltip.gateways.modifiers").withStyle(ChatFormatting.RED);
					tooltips.add(comp);
					for (RandomAttributeModifier inst : gate.getWave(wave).modifiers()) {
						comp = AttributeHelper.toComponent(inst.getAttribute(), inst.createDeterministic());
						comp = Component.translatable("tooltip.gateways.list2", comp.getString()).withStyle(ChatFormatting.RED);
						tooltips.add(comp);
					}
				}
				comp = Component.translatable("tooltip.gateways.rewards").withStyle(ChatFormatting.GOLD);
				tooltips.add(comp);
				for (Reward r : gate.getWave(wave).rewards()) {
					r.appendHoverText(c -> {
						tooltips.add(Component.translatable("tooltip.gateways.list2", c).withStyle(ChatFormatting.GOLD));
					});
				}
			} else {
				comp = Component.translatable("tooltip.gateways.shift").withStyle(ChatFormatting.GREEN);
				tooltips.add(comp);
			}
			if (Screen.hasControlDown()) {
				comp = Component.translatable("tooltip.gateways.completion").withStyle(Style.EMPTY.withColor(0xFCFF00).withUnderlined(true));
				tooltips.add(comp);
				tooltips.add(Component.nullToEmpty(null));
				comp = Component.translatable("tooltip.gateways.experience", gate.getCompletionXp()).withStyle(Style.EMPTY.withColor(0xFCFF00));
				tooltips.add(comp);
				for (Reward r : gate.getRewards()) {
					r.appendHoverText(c -> {
						tooltips.add(Component.translatable("tooltip.gateways.list2", c).withStyle(Style.EMPTY.withColor(0xFCFF00)));
					});
				}
			} else {
				comp = Component.translatable("tooltip.gateways.ctrl").withStyle(Style.EMPTY.withColor(0xFCFF00));
				tooltips.add(comp);
			}
			if (!gate.getFailures().isEmpty()) {
				if (Screen.hasAltDown()) {
					comp = Component.translatable("tooltip.gateways.failure").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withUnderlined(true));
					tooltips.add(comp);
					tooltips.add(Component.nullToEmpty(null));
					for (Failure f : gate.getFailures()) {
						f.appendHoverText(c -> {
							tooltips.add(Component.translatable("tooltip.gateways.list2", c).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
						});
					}
				} else {
					comp = Component.translatable("tooltip.gateways.alt").withStyle(ChatFormatting.DARK_RED);
					tooltips.add(comp);
				}
			}
			if (gate.playerDamageOnly()) {
				tooltips.add(Component.translatable("tooltip.gateways.player_damage_only").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
			}
			if (gate.allowsDiscarding()) {
				tooltips.add(Component.translatable("tooltip.gateways.allows_discarding").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
			}
		}
	}

	public static final ResourceLocation BARS = new ResourceLocation("textures/gui/bars.png");

	public static void bossRenderPre(CustomizeGuiOverlayEvent.BossEventProgress event) {
		BossEvent boss = event.getBossEvent();
		String name = boss.getName().getString();
		if (name.startsWith("GATEWAY_ID")) {
			Level level = Minecraft.getInstance().level;
			event.setCanceled(true);
			if (level.getEntity(Integer.valueOf(name.substring(10))) instanceof GatewayEntity gate) {
				int color = gate.getGateway().getColor().getValue();
				int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
				RenderSystem.setShaderColor(r / 255F, g / 255F, b / 255F, 1.0F);
				RenderSystem.setShaderTexture(0, BARS);
				PoseStack stack = event.getPoseStack();

				int wave = gate.getWave() + 1;
				int maxWave = gate.getGateway().getNumWaves();
				int enemies = gate.getActiveEnemies();
				int maxEnemies = gate.getCurrentWave().entities().size();

				int x = event.getX();
				int y = event.getY();
				int y2 = y + event.getIncrement();
				Gui.blit(stack, x, y, 200, 0, 6 * 5 * 2, 182, 5, 256, 256);
				Gui.blit(stack, x, y2, 200, 0, 6 * 5 * 2, 182, 5, 256, 256);

				float waveProgress = 1F / maxWave;
				float progress = waveProgress * (maxWave - wave + 1);
				if (gate.isWaveActive()) progress -= waveProgress * ((float) (maxEnemies - enemies) / maxEnemies);

				int i = (int) (progress * 183.0F);
				if (i > 0) Gui.blit(stack, x, y, 200, 0, 6 * 5 * 2 + 5, i, 5, 256, 256);

				float maxTime = gate.getCurrentWave().maxWaveTime();
				if (gate.isWaveActive()) {
					i = (int) ((maxTime - gate.getTicksActive()) / maxTime * 183.0F);
					if (i > 0) Gui.blit(stack, x, y2, 200, 0, 6 * 5 * 2 + 5, i, 5, 256, 256);
				} else {
					maxTime = gate.getCurrentWave().setupTime();
					i = (int) (gate.getTicksActive() / maxTime * 183.0F);
					if (i > 0) Gui.blit(stack, x, y2, 200, 0, 6 * 5 * 2 + 5, i, 5, 256, 256);
				}
				RenderSystem.setShaderColor(1, 1, 1, 1);
				Font font = Minecraft.getInstance().font;

				int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
				Component component = Component.literal(gate.getCustomName().getString()).withStyle(ChatFormatting.GOLD);
				int strWidth = font.width(component);
				int textX = width / 2 - strWidth / 2;
				int textY = y - 9;
				font.drawShadow(stack, component, textX, textY, 16777215);
				event.setIncrement(event.getIncrement() * 2);
				textY = y2 - 9;

				int time = (int) maxTime - gate.getTicksActive();
				String str = I18n.get("boss.gateways.wave", wave, maxWave, StringUtil.formatTickDuration(time), enemies);
				if (!gate.isWaveActive()) {
					if (gate.isLastWave()) {
						str = I18n.get("boss.gateways.done");
					} else str = I18n.get("boss.gateways.starting", wave, StringUtil.formatTickDuration(time));
				}
				component = Component.literal(str).withStyle(ChatFormatting.GREEN);
				strWidth = font.width(component);
				textX = width / 2 - strWidth / 2;
				font.drawShadow(stack, component, textX, textY, 16777215);
			}
		}
	}

}
