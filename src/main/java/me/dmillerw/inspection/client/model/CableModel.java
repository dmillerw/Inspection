package me.dmillerw.inspection.client.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import me.dmillerw.inspection.block.BlockCable;
import me.dmillerw.inspection.block.property.ConnectionType;
import me.dmillerw.inspection.lib.ModInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.dmillerw.inspection.block.BlockCable.*;

/**
 * @author dmillerw
 */
public class CableModel implements IBakedModel {

    private TextureAtlasSprite textureCableSide;
    private TextureAtlasSprite textureCableEnd;

    private VertexFormat format;

    public CableModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
        this.textureCableSide = bakedTextureGetter.apply(new ResourceLocation(ModInfo.MOD_ID, "blocks/cable"));
        this.textureCableEnd = bakedTextureGetter.apply(new ResourceLocation(ModInfo.MOD_ID, "blocks/cable_end"));
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, Vec3d vertex, float u, float v, TextureAtlasSprite sprite) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float) vertex.xCoord, (float) vertex.yCoord, (float) vertex.zCoord, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.xCoord, (float) normal.yCoord, (float) normal.zCoord, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        return createQuad(v1, v2, v3, v4, sprite, 0);
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite, int rotateUV) {
        Vec3d normal = v1.subtract(v2).crossProduct(v3.subtract(v2));
        normal = normal.normalize().rotatePitch(180).rotateYaw(180);

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);

        switch (rotateUV) {
            case 3: {
                putVertex(builder, normal, v1, 16, 0, sprite);
                putVertex(builder, normal, v2, 16, 16, sprite);
                putVertex(builder, normal, v3, 0, 16, sprite);
                putVertex(builder, normal, v4, 0, 0, sprite);
                break;
            }
            case 2: {
                putVertex(builder, normal, v1, 16, 16, sprite);
                putVertex(builder, normal, v2, 0, 16, sprite);
                putVertex(builder, normal, v3, 0, 0, sprite);
                putVertex(builder, normal, v4, 16, 0, sprite);
                break;
            }
            case 1: {
                putVertex(builder, normal, v1, 0, 16, sprite);
                putVertex(builder, normal, v2, 0, 0, sprite);
                putVertex(builder, normal, v3, 16, 16, sprite);
                putVertex(builder, normal, v4, 16, 0, sprite);
                break;
            }
            default: {
                putVertex(builder, normal, v1, 0, 0, sprite);
                putVertex(builder, normal, v2, 0, 16, sprite);
                putVertex(builder, normal, v3, 16, 16, sprite);
                putVertex(builder, normal, v4, 16, 0, sprite);
                break;
            }
        }

        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {

        if (side != null) {
            return Collections.emptyList();
        }

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;

        ConnectionType north = extendedBlockState.getValue(BlockCable.NORTH);
        ConnectionType south = extendedBlockState.getValue(BlockCable.SOUTH);
        ConnectionType west = extendedBlockState.getValue(BlockCable.WEST);
        ConnectionType east = extendedBlockState.getValue(BlockCable.EAST);
        ConnectionType up = extendedBlockState.getValue(BlockCable.UP);
        ConnectionType down = extendedBlockState.getValue(BlockCable.DOWN);

        List<BakedQuad> quads = new ArrayList<>();

        // Y - 1 - 0
        if (up.renderCable()) {
            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1, 1 - CABLE_SIZE),
                    textureCableSide, 2));

            if (up.renderConnector()) {
                // CONNECTOR
                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 1, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 1, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 1, 1 - CONNECTOR_SIZE),
                        textureCableEnd, 2));

                // CONNECTOR CAP
                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 0.999, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0.999, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0.999, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 0.999, CONNECTOR_SIZE),
                        textureCableEnd));
            }
        } else {
            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    textureCableEnd));
        }

        // Y - 0 - 1
        if (down.renderCable()) {
            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, 0, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 0, 1 - CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 0, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 0, CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 0, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 0, CABLE_SIZE),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 0, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 0, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide, 2));

            if (down.renderConnector()) {
                // CONNECTOR
                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_SIZE, 0, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 0, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 0, CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 0, CONNECTOR_SIZE),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 0, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        textureCableEnd, 2));

                // CONNECTOR CAP
                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 0.001, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0.001, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, 0.001, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, 0.001, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_DEPTH, CONNECTOR_SIZE),
                        textureCableEnd));
            }
        } else {
            quads.add(createQuad(new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE), new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE), new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE), new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE), textureCableEnd));
        }

        // X - 0 - 1
        if (east.renderCable()) {
            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide));

            if (east.renderConnector()) {
                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_DEPTH, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_DEPTH, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(0.999, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(0.999, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(0.999, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(0.999, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_DEPTH, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(1 - CONNECTOR_DEPTH, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        textureCableEnd));
            }
        } else {
            quads.add(createQuad(new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE), new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE), new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE), new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE), textureCableEnd));
        }

        if (west.renderCable()) {
            quads.add(createQuad(
                    new Vec3d(0, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(0, 1 - CABLE_SIZE, CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(0, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(0, CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(0, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(0, CABLE_SIZE, CABLE_SIZE),
                    textureCableSide));

            quads.add(createQuad(
                    new Vec3d(0, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(0, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide));

            if (west.renderConnector()) {
                quads.add(createQuad(
                        new Vec3d(0, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(0, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(0, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(0, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(0, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(0, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(0, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(0, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_DEPTH, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(CONNECTOR_DEPTH, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(0.001, CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(0.001, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE),
                        new Vec3d(0.001, 1 - CONNECTOR_SIZE, CONNECTOR_SIZE),
                        new Vec3d(0.001, CONNECTOR_SIZE, CONNECTOR_SIZE),
                        textureCableEnd));
            }
        } else {
            quads.add(createQuad(new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE), new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE), new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE), new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE), textureCableEnd));
        }

        if (north.renderCable()) {
            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 0),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 0),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 0),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 0),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 0),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 0),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 0),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 0),
                    textureCableSide, 2));

            if (north.renderConnector()) {
                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 0),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 0),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 0),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 0),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, CONNECTOR_DEPTH),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0.001),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0.001),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 0.001),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 0.001),
                        textureCableEnd));
            }
        } else {
            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, CABLE_SIZE),
                    textureCableEnd));
        }

        if (south.renderCable()) {
            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1),
                    textureCableSide, 2));

            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableSide, 2));

            if (south.renderConnector()) {
                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 1),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 1),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 1),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 1),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        textureCableEnd, 2));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 1 - CONNECTOR_DEPTH),
                        textureCableEnd));

                quads.add(createQuad(
                        new Vec3d(CONNECTOR_SIZE, CONNECTOR_SIZE, 0.999),
                        new Vec3d(1 - CONNECTOR_SIZE, CONNECTOR_SIZE, 0.999),
                        new Vec3d(1 - CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0.999),
                        new Vec3d(CONNECTOR_SIZE, 1 - CONNECTOR_SIZE, 0.999),
                        textureCableEnd));
            }
        } else {
            quads.add(createQuad(
                    new Vec3d(CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(1 - CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    new Vec3d(CABLE_SIZE, 1 - CABLE_SIZE, 1 - CABLE_SIZE),
                    textureCableEnd));
        }

        return quads;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return textureCableEnd;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    public static class Loader implements IModel {

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            return new CableModel(state, format, bakedTextureGetter);
        }

        @Override
        public Collection<ResourceLocation> getDependencies() {
            return Collections.emptySet();
        }

        @Override
        public Collection<ResourceLocation> getTextures() {
            return ImmutableSet.of(
                    new ResourceLocation(ModInfo.MOD_ID, "blocks/cable"),
                    new ResourceLocation(ModInfo.MOD_ID, "blocks/cable_end")
            );
        }

        @Override
        public IModelState getDefaultState() {
            return TRSRTransformation.identity();
        }
    }
}