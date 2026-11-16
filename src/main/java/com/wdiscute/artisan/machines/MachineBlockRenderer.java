package com.wdiscute.artisan.machines;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wdiscute.artisan.ArtisanConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public class MachineBlockRenderer implements BlockEntityRenderer<AbstractMachineBlockEntity>
{
    final BlockRenderDispatcher blockRenderer;

    public MachineBlockRenderer(BlockEntityRendererProvider.Context context)
    {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    public static float[] squish(BlockPos pos, float partialTick, boolean upgraded)
    {
        double intensity = upgraded ? ArtisanConfig.BOB_AMOUNT_UPGRADED.get() : ArtisanConfig.BOB_AMOUNT.get();
        double angle = ((Minecraft.getInstance().gui.getGuiTicks() + (double) partialTick) + pos.getX() + pos.getZ()) * 0.3;
        float s = Mth.sin((float) (angle % (Math.PI * 2)));
        float sy = (float) (1f + s * 0.1f * intensity);
        float sxz = 1f / Mth.sqrt(sy);
        float hop = (float) (Math.max(0f, s) * 0.05f * intensity);
        return new float[]{sxz, sy, hop};
    }

    @Override
    public void render(AbstractMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        Level level = blockEntity.getLevel();
        BlockState state = blockEntity.getBlockState();

        if (level == null || state.isAir())
            return;

        if (!blockEntity.isWorking())
            return;

        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();

        //very squishy machine
        float[] a = squish(pos, partialTick, !blockEntity.upgrades.isEmpty());
        poseStack.translate(0.5, a[2], 0.5);
        poseStack.scale(a[0], a[1], a[0]);
        poseStack.translate(-0.5, 0, -0.5);

        RandomSource random = RandomSource.create(state.getSeed(pos));
        BakedModel model = blockRenderer.getBlockModel(state);

        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY))
        {
            blockRenderer.getModelRenderer().tesselateBlock(
                    level,
                    model,
                    state,
                    pos.above(),
                    poseStack,
                    bufferSource.getBuffer(RenderTypeHelper.getMovingBlockRenderType(renderType)),
                    true,
                    random,
                    state.getSeed(pos),
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    renderType);
        }

        poseStack.popPose();
    }
}
