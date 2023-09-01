package dev.shadowsoffire.gateways.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@SuppressWarnings("deprecation")
public class GatewayParticle extends TextureSheetParticle {

    static final ParticleRenderType RENDER_TYPE = new ParticleRenderType(){
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            // RenderSystem.enableAlphaTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            GlStateManager._disableCull();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
            // RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() {
            return "GatewayParticleType";
        }
    };

    public GatewayParticle(GatewayParticleData data, Level world, double x, double y, double z, double velX, double velY, double velZ) {
        super((ClientLevel) world, x, y, z, velX, velY, velZ);
        this.rCol = data.red;
        this.gCol = data.green;
        this.bCol = data.blue;
        this.lifetime = 40;
        this.xd = velX;
        this.yd = velY;
        this.zd = velZ;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        RENDER_TYPE.begin((BufferBuilder) pBuffer, null);
        super.render(pBuffer, pRenderInfo, pPartialTicks);
        RENDER_TYPE.end(Tesselator.getInstance());
    }

    @Override
    protected int getLightColor(float partialTicks) {
        return LightTexture.pack(15, 15);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public float getQuadSize(float p_217561_1_) {
        return 0.75F * this.quadSize * Mth.clamp((this.age + p_217561_1_) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.alpha = 1 - (float) this.age / this.lifetime;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        else {
            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }

            this.xd *= 0.86F;
            this.yd *= 0.86F;
            this.zd *= 0.86F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }

}
