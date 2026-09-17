package com.wackyforever.chaoticbreaking;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ChaoticBreaking implements ModInitializer {
    public static final String MOD_ID = "chaoticbreaking";

    /*
     * Safety controls:
     *  - COPIES_PER_EVENT: number of copies made from each affected object.
     *  - SEARCH_RADIUS: how far from the broken block the effect reaches.
     *  - MAX_DUPLICATED_ENTITIES: hard cap for one event.
     *
     * The default is intentionally conservative for testing. Increase these
     * values if you want a more extreme recreation of the video.
     */
    private static final int COPIES_PER_EVENT = 2;
    private static final double SEARCH_RADIUS = 6.0;
    private static final int MAX_DUPLICATED_ENTITIES = 128;

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return;
            if (player.isSpectator()) return;

            multiplyNearby(serverWorld, Vec3d.ofCenter(pos));
        });
    }

    private static void multiplyNearby(ServerWorld world, Vec3d center) {
        Box box = new Box(
                center.x - SEARCH_RADIUS, center.y - SEARCH_RADIUS, center.z - SEARCH_RADIUS,
                center.x + SEARCH_RADIUS, center.y + SEARCH_RADIUS, center.z + SEARCH_RADIUS
        );

        List<Entity> originals = new ArrayList<>(world.getOtherEntities(null, box));
        int created = 0;

        for (Entity original : originals) {
            if (created >= MAX_DUPLICATED_ENTITIES) break;
            if (!isEligible(original)) continue;

            for (int i = 0; i < COPIES_PER_EVENT && created < MAX_DUPLICATED_ENTITIES; i++) {
                Entity copy = duplicateEntity(world, original);
                if (copy != null) {
                    Vec3d p = original.getPos();
                    double ox = (world.random.nextDouble() - 0.5) * 1.5;
                    double oy = world.random.nextDouble() * 0.8;
                    double oz = (world.random.nextDouble() - 0.5) * 1.5;
                    copy.refreshPositionAndAngles(
                            p.x + ox, p.y + oy, p.z + oz,
                            original.getYaw(), original.getPitch()
                    );
                    world.spawnEntity(copy);
                    created++;
                }
            }
        }
    }

    private static boolean isEligible(Entity entity) {
        // Dropped items and normal living/interactive entities are eligible.
        // Players are excluded to prevent accidental player duplication.
        return !(entity instanceof net.minecraft.entity.player.Player);
    }

    private static Entity duplicateEntity(ServerWorld world, Entity original) {
        if (original instanceof ItemEntity item) {
            ItemStack stack = item.getStack().copy();
            ItemEntity copy = new ItemEntity(world, item.getX(), item.getY(), item.getZ(), stack);
            copy.setVelocity(item.getVelocity());
            return copy;
        }

        EntityType<?> type = original.getType();
        Entity copy = type.create(world);
        if (copy == null) return null;

        // Entity#create gives a fresh entity of the same type. This intentionally
        // avoids copying UUIDs and world-specific state.
        return copy;
    }
}
