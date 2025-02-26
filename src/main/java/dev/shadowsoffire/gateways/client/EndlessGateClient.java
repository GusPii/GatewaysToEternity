package dev.shadowsoffire.gateways.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apothic_attributes.api.AttributeHelper;
import dev.shadowsoffire.gateways.entity.EndlessGatewayEntity;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import dev.shadowsoffire.gateways.gate.endless.EndlessGateway;
import dev.shadowsoffire.gateways.gate.endless.EndlessModifier;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

/**
 * Client code specific to {@link NormalGateway}
 */
public class EndlessGateClient {

    public static void appendPearlTooltip(EndlessGateway gate, TooltipContext ctx, List<Component> tooltips, TooltipFlag flag) {
        MutableComponent comp;

        Wave wave = gate.baseWave();
        comp = Component.translatable("tooltip.gateways.endless.base_wave").withStyle(ChatFormatting.GRAY);
        tooltips.add(comp);

        comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.entities").withStyle(Style.EMPTY.withColor(0x87CEEB)));
        tooltips.add(comp);

        for (WaveEntity entity : wave.entities()) {
            comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", entity.getDescription()).withStyle(Style.EMPTY.withColor(0x87CEEB)));
            tooltips.add(comp);
        }

        if (!wave.modifiers().isEmpty()) {
            comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.modifiers").withStyle(ChatFormatting.RED));
            tooltips.add(comp);
            for (WaveModifier modif : wave.modifiers()) {
                modif.appendHoverText(ctx, c -> {
                    tooltips.add(AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", c.withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.RED)));
                });
            }
        }

        comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.rewards").withStyle(ChatFormatting.GOLD));
        tooltips.add(comp);
        for (Reward r : wave.rewards()) {
            r.appendHoverText(ctx, c -> {
                tooltips.add(AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", c).withStyle(ChatFormatting.GOLD)));
            });
        }

        int modifIdx = Math.floorMod(GatewaysClient.scrollIdx, gate.modifiers().size());
        EndlessModifier modif = gate.modifiers().get(modifIdx);

        if (Screen.hasShiftDown()) {
            comp = Component.translatable("tooltip.gateways.endless.modifier", modifIdx + 1, gate.modifiers().size()).withStyle(ChatFormatting.LIGHT_PURPLE);
            comp.append(CommonComponents.SPACE);
            comp.append(Component.translatable("tooltip.gateways.scroll").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withUnderlined(false)));
            tooltips.add(comp);

            comp = AttributeHelper.list().append(modif.appMode().getDescription().withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltips.add(comp);

            if (modif.waveTime() != 0) {
                String value = modif.waveTime() > 0 ? "+" + modif.waveTime() : String.valueOf(modif.waveTime());
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.endless.wave_time", value).withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltips.add(comp);
            }

            if (modif.setupTime() != 0) {
                String value = modif.setupTime() > 0 ? "+" + modif.setupTime() : String.valueOf(modif.setupTime());
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.endless.setup_time", value).withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltips.add(comp);
            }

            if (!modif.entities().isEmpty()) {
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.entities").withStyle(Style.EMPTY.withColor(0x87CEEB)));
                tooltips.add(comp);
                for (WaveEntity entity : modif.entities()) {
                    comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", entity.getDescription()).withStyle(Style.EMPTY.withColor(0x87CEEB)));
                    tooltips.add(comp);
                }
            }

            if (!modif.modifiers().isEmpty()) {
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.modifiers").withStyle(ChatFormatting.RED));
                tooltips.add(comp);
                for (WaveModifier waveModif : modif.modifiers()) {
                    waveModif.appendHoverText(ctx, c -> {
                        tooltips.add(AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", c.withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.RED)));
                    });
                }
            }

            if (!modif.rewards().isEmpty()) {
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.rewards").withStyle(ChatFormatting.GOLD));
                tooltips.add(comp);
                for (Reward r : modif.rewards()) {
                    r.appendHoverText(ctx, c -> {
                        tooltips.add(AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", c).withStyle(ChatFormatting.GOLD)));
                    });
                }
            }
        }
        else {
            comp = Component.translatable("tooltip.gateways.endless.num_modif" + (gate.modifiers().size() == 1 ? "" : "s"), gate.modifiers().size()).withStyle(ChatFormatting.LIGHT_PURPLE);
            comp.append(CommonComponents.SPACE);
            comp.append(Component.translatable("tooltip.gateways.shift").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
            tooltips.add(comp);
        }

        List<Failure> failures = gate.failures();
        if (!failures.isEmpty()) {
            if (Screen.hasControlDown()) {
                comp = Component.translatable("tooltip.gateways.failures").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                tooltips.add(comp);
                for (Failure f : failures) {
                    f.appendHoverText(ctx, c -> {
                        tooltips.add(AttributeHelper.list().append(c.withStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                    });
                }
            }
            else {
                comp = Component.translatable("tooltip.gateways.num_failure" + (failures.size() == 1 ? "" : "s"), failures.size()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                comp.append(CommonComponents.SPACE);
                comp.append(Component.translatable("tooltip.gateways.ctrl").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
                tooltips.add(comp);
            }
        }

        List<MutableComponent> deviations = gate.rules().buildDeviations();
        if (!deviations.isEmpty()) {
            if (Screen.hasAltDown()) {
                comp = Component.translatable("tooltip.gateways.rules", deviations.size()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
                tooltips.add(comp);
                deviations.forEach(c -> {
                    tooltips.add(AttributeHelper.list().append(c.withStyle(ChatFormatting.DARK_GREEN)));
                });
            }
            else {
                comp = Component.translatable("tooltip.gateways.num_rule" + (deviations.size() == 1 ? "" : "s"), deviations.size()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
                comp.append(CommonComponents.SPACE);
                comp.append(Component.translatable("tooltip.gateways.alt").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
                tooltips.add(comp);
            }
        }
    }

    public static void renderBossBar(GatewayEntity gateEntity, Object guiGfx, int x, int y, boolean isInWorld) {
        EndlessGatewayEntity gate = (EndlessGatewayEntity) gateEntity;
        GuiGraphics gfx = (GuiGraphics) guiGfx;
        PoseStack pose = gfx.pose();
        int color = gate.getGateway().color().getValue();
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        RenderSystem.setShaderColor(r / 255F, g / 255F, b / 255F, 1.0F);

        int wave = gate.getWave() + 1;
        int enemies = gate.getActiveEnemies();
        int maxEnemies = gate.getMaxEnemies();
        int modifiers = gate.getModifiersApplied();
        int lineHeight = Minecraft.getInstance().font.lineHeight;

        int yBar1 = y + 6 + lineHeight;
        int yBar2 = yBar1 + 10 + lineHeight;
        int textY = y - lineHeight;

        pose.pushPose();
        pose.translate(0, 0, 0.01);
        gfx.blitSprite(GatewaysClient.WHITE_BACKGROUND, x, yBar1, 182, 5);
        gfx.blitSprite(GatewaysClient.WHITE_BACKGROUND, x, yBar2, 182, 5);
        pose.popPose();

        int barWidth = 183;

        float maxTime = gate.getMaxWaveTime();
        if (gate.isWaveActive()) {
            barWidth = (int) (183.0F * enemies / maxEnemies);
            if (barWidth > 0) {
                gfx.blitSprite(GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, yBar1, barWidth, 5);
            }

            barWidth = (int) ((maxTime - gate.getTicksActive()) / maxTime * 183.0F);
            if (barWidth > 0) {
                gfx.blitSprite(GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, yBar2, barWidth, 5);
            }
        }
        else {
            maxTime = gate.getSetupTime();
            barWidth = (int) (gate.getTicksActive() / maxTime * 183.0F);
            if (barWidth > 0) {
                gfx.blitSprite(GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, yBar1, barWidth, 5);
                gfx.blitSprite(GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, yBar2, barWidth, 5);
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        Font font = Minecraft.getInstance().font;

        Component component = Component.literal(gate.getCustomName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE);
        int strWidth = font.width(component);
        int textX = x + 182 / 2 - strWidth / 2;
        if (isInWorld) {
            GatewaysClient.drawReversedDropShadow(gfx, font, component, textX, textY);
        }
        else {
            gfx.drawString(font, component, textX, textY, 16777215, true);
        }

        int time = (int) maxTime - gate.getTicksActive();
        float tps = gateEntity.level().tickRateManager().tickrate();
        String str = I18n.get("boss.gateways.endless.top", wave, '\u221E', StringUtil.formatTickDuration(time, tps));
        String str2 = I18n.get("boss.gateways.endless.bot", enemies, maxEnemies, modifiers);
        if (!gate.isWaveActive()) {
            str = I18n.get("boss.gateways.starting", wave, StringUtil.formatTickDuration(time, tps));
            str2 = I18n.get("boss.gateways.endless.incoming", maxEnemies);
        }
        component = Component.literal(str).withStyle(ChatFormatting.GREEN);
        strWidth = font.width(component);
        textX = x + 182 / 2 - strWidth / 2;
        textY = yBar1 - lineHeight;
        if (isInWorld) {
            GatewaysClient.drawReversedDropShadow(gfx, font, component, textX, textY);
        }
        else {
            gfx.drawString(font, component, textX, textY, 16777215, true);
        }

        component = Component.literal(str2).withStyle(ChatFormatting.GREEN);
        strWidth = font.width(component);
        textX = x + 182 / 2 - strWidth / 2;
        textY = yBar2 - lineHeight;
        if (isInWorld) {
            GatewaysClient.drawReversedDropShadow(gfx, font, component, textX, textY);
        }
        else {
            gfx.drawString(font, component, textX, textY, 16777215, true);
        }
    }

}
