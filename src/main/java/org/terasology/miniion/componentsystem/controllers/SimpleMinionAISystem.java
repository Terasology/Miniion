///*
// * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.terasology.miniion.componentsystem.controllers;
//
//import java.util.List;
//
//import javax.vecmath.AxisAngle4f;
//import javax.vecmath.Vector3f;
//
//import org.terasology.engine.CoreRegistry;
//import org.terasology.engine.Time;
//import org.terasology.entitySystem.entity.EntityManager;
//import org.terasology.entitySystem.entity.EntityRef;
//import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
//import org.terasology.entitySystem.event.ReceiveEvent;
//import org.terasology.entitySystem.systems.ComponentSystem;
//import org.terasology.entitySystem.systems.In;
//import org.terasology.entitySystem.systems.RegisterMode;
//import org.terasology.entitySystem.systems.RegisterSystem;
//import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
//import org.terasology.logic.characters.CharacterMovementComponent;
//import org.terasology.logic.characters.events.HorizontalCollisionEvent;
//import org.terasology.logic.health.DoDamageEvent;
//import org.terasology.logic.health.EngineDamageTypes;
//import org.terasology.logic.inventory.InventoryComponent;
//import org.terasology.logic.inventory.ItemComponent;
//import org.terasology.logic.location.LocationComponent;
//import org.terasology.logic.players.LocalPlayer;
//import org.terasology.math.Vector3i;
//import org.terasology.miniion.components.AnimationComponent;
//import org.terasology.miniion.components.MinionComponent;
//import org.terasology.miniion.components.MinionFarmerComponent;
//import org.terasology.miniion.components.NPCMovementInputComponent;
//import org.terasology.miniion.components.SimpleMinionAIComponent;
//import org.terasology.miniion.events.MinionMessageEvent;
//import org.terasology.miniion.minionenum.MinionMessagePriority;
//import org.terasology.miniion.pathfinder.AStarPathing;
//import org.terasology.miniion.utilities.MinionMessage;
//import org.terasology.rendering.assets.animation.MeshAnimation;
//import org.terasology.rendering.logic.SkeletalMeshComponent;
//import org.terasology.world.BlockEntityRegistry;
//import org.terasology.world.WorldProvider;
//import org.terasology.world.block.Block;
//import org.terasology.world.block.BlockManager;
//import org.terasology.world.block.items.BlockItemComponent;
//import org.terasology.world.block.items.BlockItemFactory;
//import org.terasology.zone.ZoneComponent;
//import org.terasology.zone.ZoneType;
//
///**
// * Created with IntelliJ IDEA. User: Overdhose Date: 7/05/12 Time: 18:25 first
// * evolution of the minion AI, could probably use a lot of improvements
// */
//@RegisterSystem(RegisterMode.AUTHORITY)
//public class SimpleMinionAISystem implements ComponentSystem,
//        UpdateSubscriberSystem {
//    private static final String DEFAULT_TERRAFORM_FINAL_BLOCK_TYPE_NAME = "CakeLie:ChocolateBlock";
//    private static final String DEFAULT_CROP_BLOCK_NAME = "core:plant";
//
//    @In
//    private BlockManager blockManager;
//
////    @In
////    private SlotBasedInventoryManager inventoryManager;
//
//    @In
//    private EntityManager entityManager;
//    @In
//    private WorldProvider worldProvider;
//    @In
//    private BlockEntityRegistry blockEntityRegistry;
//    @In
//    private Time timer;
//
//    private AStarPathing aStarPathing;
//
//    @Override
//    public void initialise() {
//        aStarPathing = new AStarPathing(worldProvider);
//
//    }
//
//    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
//    public void onSpawn(OnAddedComponent event, EntityRef entity) {
//        initMinionAI();
//    }
//
//    private void initMinionAI() {
//        //add 3000 to init to create  bit of a delay before first check
//        long initTime = timer.getGameTimeInMs() + 3000;
//        for (EntityRef minion : entityManager.getEntitiesWith(SimpleMinionAIComponent.class)) {
//            SimpleMinionAIComponent ai = minion.getComponent(SimpleMinionAIComponent.class);
//            ai.lastAttacktime = initTime;
//            ai.lastDistancecheck = initTime;
//            ai.lastHungerCheck = initTime;
//            minion.saveComponent(ai);
//        }
//    }
//
//    @Override
//    public void shutdown() {
//    }
//
//    @Override
//    public void update(float delta) {
//        for (EntityRef entity : entityManager.getEntitiesWith(
//                SimpleMinionAIComponent.class,
//                NPCMovementInputComponent.class, LocationComponent.class,
//                MinionComponent.class, SkeletalMeshComponent.class,
//                AnimationComponent.class)) {
//
//            MinionComponent minioncomp = entity
//                    .getComponent(MinionComponent.class);
//            AnimationComponent animcomp = entity
//                    .getComponent(AnimationComponent.class);
//            SimpleMinionAIComponent ai = entity
//                    .getComponent(SimpleMinionAIComponent.class);
//
//            //hunger system, increase the delay by increasing > 10000
//            if (timer.getGameTimeInMs() - ai.lastHungerCheck > 10000) {
//                ai.lastHungerCheck = timer.getGameTimeInMs();
//                if (minioncomp.Hunger > 0) {
//                    minioncomp.Hunger--;
//                    //need to save components for data to persist when game restarts
//                    entity.saveComponent(minioncomp);
//                } else {
//                    //die? reset for now so you see effect
//                    minioncomp.Hunger = 100;
//                }
//            }
//
//            switch (minioncomp.minionBehaviour) {
//                case Follow: {
//                    executeFollowAI(entity);
//                    break;
//                }
//                case Gather: {
//                    executeGatherAI(entity);
//                    break;
//                }
//                case Work: {
//                    executeWorkAI(entity);
//                    break;
//                }
//                case Terraform: {
//                    executeTerraformAI(entity, "");
//                    break;
//                }
//                case Move: {
//                    executeMoveAI(entity);
//                    break;
//                }
//                case Patrol: {
//                    executePatrolAI(entity);
//                    break;
//                }
//                case Attack: {
//                    changeAnimation(entity, animcomp.attackAnim, true);
//                    break;
//                }
//                case Die: {
//                    changeAnimation(entity, animcomp.dieAnim, false);
//                    break;
//                }
//                case Stay: {
//                    executeStayAI(entity);
//                    break;
//                }
//                case Test: {
//                    executeTestAI(entity);
//                    break;
//                }
//                default: {
//                    changeAnimation(entity, animcomp.idleAnim, false);
//                    break;
//                }
//            }
//        }
//    }
//
//    private void executeStayAI(EntityRef entity) {
//        NPCMovementInputComponent movementInput = entity
//                .getComponent(NPCMovementInputComponent.class);
//        movementInput.directionToMove = new Vector3f(0, 0, 0);
//        entity.saveComponent(movementInput);
//        AnimationComponent animcomp = entity
//                .getComponent(AnimationComponent.class);
//        changeAnimation(entity, animcomp.idleAnim, false);
//    }
//
//    private void changeAnimation(EntityRef entity, MeshAnimation animation,
//                                 boolean loop) {
//        SkeletalMeshComponent skeletalcomp = entity
//                .getComponent(SkeletalMeshComponent.class);
//        if (skeletalcomp.animation != animation) {
//            skeletalcomp.animation = animation;
//            skeletalcomp.loop = loop;
//            entity.saveComponent(skeletalcomp);
//        }
//    }
//
//    private void executeFollowAI(EntityRef entity) {
//        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
//        if (localPlayer == null) {
//            return;
//        }
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(localPlayer.getPosition());
//        double distanceToPlayer = dist.lengthSquared();
//
//        if (distanceToPlayer > 8) {
//            // Head to player
//            Vector3f target = localPlayer.getPosition();
//            ai.movementTarget.set(target);
//            ai.followingPlayer = true;
//            entity.saveComponent(ai);
//        }
//
//        setMovement(ai.movementTarget, entity);
//    }
//
//    private void executeGatherAI(EntityRef entity) {
//        MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        AnimationComponent animcomp = entity
//                .getComponent(AnimationComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//        ZoneComponent assignedZoneComponent = minioncomp.assignedZoneEntity.getComponent(ZoneComponent.class);
//
//        if ((ai.gatherTargets.size() == 0) && (minioncomp.assignedZoneEntity != EntityRef.NULL) && (assignedZoneComponent.zonetype == ZoneType.Gather)) {
//            getTargetsfromZone(minioncomp, ai);
//        }
//
//        if ((ai.gatherTargets == null) || (ai.gatherTargets.size() < 1)) {
//            return;
//        }
//        Vector3f currentTarget = ai.gatherTargets.get(0);
//        if (currentTarget == null) {
//            ai.gatherTargets.remove(currentTarget);
//            changeAnimation(entity, animcomp.idleAnim, true);
//            entity.saveComponent(ai);
//            return;
//        }
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(currentTarget);
//        double distanceToTarget = dist.lengthSquared();
//
//        if (distanceToTarget < 4) {
//            // switch animation
//            changeAnimation(entity, animcomp.workAnim, false);
//            // gather the block
//            if (timer.getGameTimeInMs() - ai.lastAttacktime > 500) {
//                ai.lastAttacktime = timer.getGameTimeInMs();
//                boolean attacked = attack(entity, currentTarget);
//                if (!attacked) {
//                    changeAnimation(entity, animcomp.idleAnim, true);
//                    ai.gatherTargets.remove(currentTarget);
//                }
//            }
//        }
//
//        entity.saveComponent(ai);
//        setMovement(currentTarget, entity);
//    }
//
//    private void getTargetsfromZone(MinionComponent minioncomp,
//                                    SimpleMinionAIComponent ai) {
//        EntityRef zone = minioncomp.assignedZoneEntity;
//        ZoneComponent zoneComponent = zone.getComponent(ZoneComponent.class);
//        // first loop at highest blocks (y)
//        for (int y = zoneComponent.getMaxBounds().y; y >= zoneComponent.getMinBounds().y; y--) {
//            for (int x = zoneComponent.getMinBounds().x; x <= zoneComponent.getMaxBounds().x; x++) {
//                for (int z = zoneComponent.getMinBounds().z; z <= zoneComponent.getMaxBounds().z; z++) {
//                    Block tmpblock = worldProvider.getBlock(x, y, z);
//                    if (!tmpblock.isInvisible()) {
//                        ai.gatherTargets.add(new Vector3f(x, y + 0.5f, z));
//                    }
//                }
//            }
//        }
//    }
//
//    private void executeWorkAI(EntityRef entity) {
//        MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
//        MinionFarmerComponent minionFarmer = entity.getComponent(MinionFarmerComponent.class);
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        AnimationComponent animcomp = entity
//                .getComponent(AnimationComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//        ZoneComponent assignedZoneComponent = minioncomp.assignedZoneEntity.getComponent(ZoneComponent.class);
//
//        if (minioncomp.assignedZoneEntity == EntityRef.NULL) {
//            changeAnimation(entity, animcomp.idleAnim, true);
//            return;
//        } else if (assignedZoneComponent.zonetype != ZoneType.Work) {
//            if (assignedZoneComponent.zonetype == ZoneType.OreonFarm) {
//                if (isTerraformComplete(minioncomp.assignedZoneEntity, minionFarmer.farmFieldBlockName)) {
//                    //farming
//                    executeFarmAI(entity);
//                } else {
//                    executeTerraformAI(entity, minionFarmer.farmFieldBlockName);
//                }
//            } else {
//                changeAnimation(entity, animcomp.idleAnim, true);
//
//            }
//            return;
//        }
//
//        // TODO: crafting doesn't work
//        // TODO: inventory management has to be updated to new system
//        
////        Vector3f currentTarget = assignedZoneComponent.getStartPosition().toVector3f();
////        currentTarget.y += 0.5;
////
////        Vector3f dist = new Vector3f(worldPos);
////        dist.sub(currentTarget);
////        double distanceToTarget = dist.lengthSquared();
////
////        if (distanceToTarget < 4) {
////
////            // gather the block
////            if (timer.getGameTimeInMs() - ai.lastAttacktime > 1000) {
////                ai.lastAttacktime = timer.getGameTimeInMs();
////                //increase craft progress
////                boolean hasResources = false;
////                if (minioncomp.assignedrecipe != null) {
////                    InventoryComponent invcomp = entity.getComponent(InventoryComponent.class);
////                    for (String simpleuri : minioncomp.assignedrecipe.craftRes) {
////                        hasResources = false;
////                        // TODO: replace this with inventory iterator once the engine supports it
////                        int numSlots = inventoryManager.getNumSlots(entity);
////                        for (int slotIndex = 0; slotIndex < numSlots; slotIndex++) {
////                            EntityRef itemInSlotEntity = inventoryManager.getItemInSlot(entity, slotIndex);
////                            if ((itemInSlotEntity != null) && (EntityRef.NULL != itemInSlotEntity)) {
////                                BlockItemComponent blockItem = itemInSlotEntity.getComponent(BlockItemComponent.class);
////                                if (blockItem != null) {
////                                    if (blockItem.blockFamily.getURI().getFamilyName().matches(simpleuri)) {
////                                        hasResources = true;
////                                        break;
////                                    }
////                                }
////                            }
////                        }
////                        if (!hasResources) {
////                            break;
////                        }
////                    }
////                    if (hasResources) {
////                        // switch animation
////                        changeAnimation(entity, animcomp.workAnim, false);
////                        ai.craftprogress++;
////                        if (ai.craftprogress >= minioncomp.assignedrecipe.craftsteps) {
////                            for (String simpleuri : minioncomp.assignedrecipe.craftRes) {
////                                // TODO: replace this with inventory iterator once the engine supports it
////                                int numSlots = inventoryManager.getNumSlots(entity);
////                                for (int slotIndex = 0; slotIndex < numSlots; slotIndex++) {
////                                    EntityRef itemInSlotEntity = inventoryManager.getItemInSlot(entity, slotIndex);
////                                    if ((itemInSlotEntity != null) && (EntityRef.NULL != itemInSlotEntity)) {
////                                        BlockItemComponent blockItem = itemInSlotEntity.getComponent(BlockItemComponent.class);
////                                        ItemComponent item = itemInSlotEntity.getComponent(ItemComponent.class);
////                                        if (blockItem != null) {
////                                            if (blockItem.blockFamily.getURI().getFamilyName().matches(simpleuri)) {
////                                                if (item.stackCount >= minioncomp.assignedrecipe.quantity)
////                                                {
////                                                    item.stackCount -= minioncomp.assignedrecipe.quantity;
////                                                    if (item.stackCount == 0) {
////                                                        itemInSlotEntity.destroy();
////                                                    }
////                                                    break;
////                                                }
////                                            }
////                                        }
////                                    }
////                                }
////                            }
////                            Block recipeBlock = null;
////                            EntityRef result = EntityRef.NULL;
////                            BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
////                            result = blockFactory.newInstance(blockManager.getBlockFamily(minioncomp.assignedrecipe.result));
////                            entity.send(new ReceivedItemEvent(result));
////                            ai.craftprogress = 0;
////                        }
////                    }
////                }
////            }
////        }
////
////        entity.saveComponent(ai);
////        setMovement(currentTarget, entity);
//    }
//
//    private boolean isTerraformComplete(EntityRef zone, String blockTypeName) {
//        return isZoneComposedOnlyOfBlockType(zone, blockTypeName);
//    }
//
//    private boolean isZoneComposedOnlyOfBlockType(EntityRef zone, String blockTypeName) {
//        ZoneComponent zoneComponent = zone.getComponent(ZoneComponent.class);
//        for (int y = zoneComponent.getMaxBounds().y; y >= zoneComponent.getMinBounds().y; y--) {
//            for (int x = zoneComponent.getMinBounds().x; x <= zoneComponent.getMaxBounds().x; x++) {
//                for (int z = zoneComponent.getMinBounds().z; z <= zoneComponent.getMaxBounds().z; z++) {
//                    Block block = worldProvider.getBlock(x, y, z);
//                    if (!block.getURI().toString().toLowerCase().equals(blockTypeName.toLowerCase())) {
//                        return false;
//                    }
//                }
//            }
//        }
//        
//        return true;
//    }
//
//    /**
//     * Terraforms a zone into a set recipe, by default chocolate.
//     * @param entity 
//     *                              the minion that is terraforming
//     * @param terraformFinalBlockType
//     *                              set to empty string by default for normal terraforming, 
//     *                              can override the default recipe for farming. 
//     */
//    private void executeTerraformAI(EntityRef entity, String terraformFinalBlockType) {
//        MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        AnimationComponent animcomp = entity
//                .getComponent(AnimationComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//        ZoneComponent assignedZoneComponent = minioncomp.assignedZoneEntity.getComponent(ZoneComponent.class);
//
//        if (minioncomp == null) {
//            changeAnimation(entity, animcomp.idleAnim, true);
//            return;
//        } else if (assignedZoneComponent.zonetype != ZoneType.Terraform && terraformFinalBlockType.isEmpty()) {
//            changeAnimation(entity, animcomp.idleAnim, true);
//            return;
//
//            // Not sure why we're checking for OreonFarm when we're in Terraform mode
//            // Maybe because we only want to terraform when we're building a farm?
//            //        } else if (minioncomp.assignedZoneComponent.zonetype != ZoneType.OreonFarm) {
//            //            changeAnimation(entity, animcomp.idleAnim, true);
//            //            return;
//        }
//
//        if (ai.movementTargets.size() == 0) {
//            // this might load more ai.movementTargets
//            getFirsBlockfromZone(minioncomp, ai);
//        }
//
//        Vector3f currentTarget = null;
//        if (ai.movementTargets.size() != 0) {
//            currentTarget = ai.movementTargets.get(0);
//        }
//
//        if (currentTarget == null) {
//            ai.movementTargets.remove(currentTarget);
//            changeAnimation(entity, animcomp.idleAnim, true);
//            entity.saveComponent(ai);
//            return;
//        }
//
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(currentTarget);
//        double distanceToTarget = dist.lengthSquared();
//
//        if (distanceToTarget < 4) {
//            // terraform
//            changeAnimation(entity, animcomp.terraformAnim, true);
//            if (timer.getGameTimeInMs() - ai.lastAttacktime > 200) {
//                ai.lastAttacktime = timer.getGameTimeInMs();
//                for (int y = (int) (currentTarget.y - 0.5); y >= assignedZoneComponent.getMinBounds().y; y--) {
//                    Block tmpblock = worldProvider.getBlock((int) currentTarget.x, y, (int) currentTarget.z);
//                    if (!tmpblock.isInvisible()) {
//                        String moduleName = tmpblock.getBlockFamily().getURI().getModuleName();
//                        // TODO: why do we care about what kinds of blocks we terraform?
//                        if ((moduleName.equals("engine")) || (moduleName.equals("core"))) {
//                            ai.craftprogress++;
//                            if (ai.craftprogress > 20) {
//                                Block newBlock;
//                                if ((null != terraformFinalBlockType) && !terraformFinalBlockType.isEmpty()) {
//                                    newBlock = blockManager.getBlock(terraformFinalBlockType);
//                                    if (!newBlock.getURI().toString().toLowerCase().equals(terraformFinalBlockType.toLowerCase())) {
//                                        // Not sure what we should do if block read fails, but we don't want air as default
//                                        newBlock = blockManager.getBlock(DEFAULT_TERRAFORM_FINAL_BLOCK_TYPE_NAME);
//                                    }
//
//                                } else if (minioncomp.assignedrecipe == null) {
//                                    // TODO: we should read this block asset name from the prefab
//                                    newBlock = blockManager.getBlock(DEFAULT_TERRAFORM_FINAL_BLOCK_TYPE_NAME);
//                                } else
//                                {
//                                    newBlock = blockManager.getBlock(minioncomp.assignedrecipe.result);
//                                }
//
//                                worldProvider.setBlock(new Vector3i(currentTarget.x, y, currentTarget.z), newBlock);
//                                ai.craftprogress = 0;
//                                if (y == assignedZoneComponent.getMinBounds().y) {
//                                    ai.movementTargets.remove(currentTarget);
//                                }
//                            }
//                            break;
//                        } else
//                        {
//                            if (y == assignedZoneComponent.getMinBounds().y) {
//                                ai.movementTargets.remove(currentTarget);
//                            }
//                        }
//                    } else
//                    {
//                        if (y == assignedZoneComponent.getMinBounds().y) {
//                            ai.movementTargets.remove(currentTarget);
//                        }
//                    }
//                }
//
//            }
//        }
//
//        entity.saveComponent(ai);
//        setMovement(currentTarget, entity);
//    }
//
//    private void getFirsBlockfromZone(MinionComponent minioncomp, SimpleMinionAIComponent ai) {
//        EntityRef assignedZoneEntity = minioncomp.assignedZoneEntity;
//        ZoneComponent assignedZoneComponent = assignedZoneEntity.getComponent(ZoneComponent.class);
//        for (int x = assignedZoneComponent.getMinBounds().x; x <= assignedZoneComponent.getMaxBounds().x; x++) {
//            for (int z = assignedZoneComponent.getMinBounds().z; z <= assignedZoneComponent.getMaxBounds().z; z++) {
//                for (int y = assignedZoneComponent.getMaxBounds().y; y >= assignedZoneComponent.getMinBounds().y; y--) {
//                    Block tmpblock = worldProvider.getBlock(x, y, z);
//                    if (!tmpblock.isInvisible()) {
//                        ai.movementTargets.add(new Vector3f(x, (y + 0.5f), z));
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * plants crops
//     * @param entity 
//     *                              the minion that is terraforming
//     * @param fixedrecipeuri
//     *                              set to empty string by default for normal terraforming, 
//     *                              can override the default recipe for farming. 
//     */
//    private void executeFarmAI(EntityRef entity) {
//        MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
//        MinionFarmerComponent minionFarmer = entity.getComponent(MinionFarmerComponent.class);
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        AnimationComponent animcomp = entity
//                .getComponent(AnimationComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//        ZoneComponent assignedZoneComponent = minioncomp.assignedZoneEntity.getComponent(ZoneComponent.class);
//
//        if (ai.movementTargets.size() == 0) {
//            getFirsBlockfromZone(minioncomp, ai);
//        }
//
//        Vector3f currentTarget = null;
//        if (ai.movementTargets.size() != 0) {
//            currentTarget = ai.movementTargets.get(0);
//        }
//
//        if (currentTarget == null) {
//            ai.movementTargets.remove(currentTarget);
//            changeAnimation(entity, animcomp.idleAnim, true);
//            entity.saveComponent(ai);
//            return;
//        }
//
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(currentTarget);
//        double distanceToTarget = dist.lengthSquared();
//
//        if (distanceToTarget < 4) {
//            // terraform
//            changeAnimation(entity, animcomp.terraformAnim, true);
//            if (timer.getGameTimeInMs() - ai.lastAttacktime > 200) {
//                ai.lastAttacktime = timer.getGameTimeInMs();
//                for (int y = (int) (currentTarget.y - 0.5); y >= assignedZoneComponent.getMinBounds().y; y--) {
//                    Block currentBlock = worldProvider.getBlock(new Vector3i(currentTarget.x, y + 1, currentTarget.z));
//                    Block plantedBlock = null;
//                    if (null != minionFarmer.blockNameToPlantAboveFarmField) {
//                        plantedBlock = blockManager.getBlock(minionFarmer.blockNameToPlantAboveFarmField);
//                        if (!plantedBlock.getURI().toString().toLowerCase().equals(minionFarmer.blockNameToPlantAboveFarmField.toLowerCase())) {
//                            // We didn't get what we asked for and probably got air instead
//                            plantedBlock = null;
//                        }
//                    }
//                    if (null == plantedBlock) {
//                        // Not sure what we should do if block read fails, but we don't want air as default
//                        plantedBlock = blockManager.getBlock(DEFAULT_CROP_BLOCK_NAME);
//                    }
//
//                    if (plantedBlock.getPrefab().equals(currentBlock.getPrefab())) {
//                        ai.movementTargets.remove(currentTarget);
//                        continue;
//                    }
//                    ai.craftprogress++;
//                    if (ai.craftprogress > 20) {
//                        worldProvider.setBlock(new Vector3i(currentTarget.x, y + 1, currentTarget.z), plantedBlock);
//                        ai.craftprogress = 0;
//                        if (y == assignedZoneComponent.getMinBounds().y) {
//                            ai.movementTargets.remove(currentTarget);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (ai.movementTargets.size() == 0) {
//            ai.movementTargets.remove(currentTarget);
//            changeAnimation(entity, animcomp.idleAnim, true);
//            entity.saveComponent(ai);
//            return;
//        }
//
//        entity.saveComponent(ai);
//        setMovement(currentTarget, entity);
//    }
//
//    private void executeMoveAI(EntityRef entity) {
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//        NPCMovementInputComponent movementInput = entity.getComponent(NPCMovementInputComponent.class);
//
//        // get targets, break if none
//        List<Vector3f> targets = ai.movementTargets;
//        if ((targets == null) || (targets.size() < 1)) {
//            movementInput.directionToMove = new Vector3f(0, 0, 0);
//            entity.saveComponent(movementInput);
//            return;
//        }
//        Vector3f currentTarget = targets.get(0);
//        // trying to solve distance calculation with some simple trick of
//        // reducing the height to 0.5, might not work for taller entities
//        worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;
//
//        // calc distance to current Target
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(currentTarget);
//        double distanceToTarget = dist.length();
//
//        // used 1.0 here as a check, should be lower to have the minion jump on
//        // the last block, TODO need to calc middle of block
//        if (distanceToTarget < 0.1d) {
//            ai.movementTargets.remove(0);
//            entity.saveComponent(ai);
//            currentTarget = null;
//            return;
//        }
//
//        setMovement(currentTarget, entity);
//    }
//
//    private void executePatrolAI(EntityRef entity) {
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//
//        // get targets, break if none
//        List<Vector3f> targets = ai.patrolTargets;
//        if ((targets == null) || (targets.size() < 1)) {
//            return;
//        }
//        int patrolCounter = ai.patrolCounter;
//        Vector3f currentTarget = null;
//
//        // get the patrol point
//        if (patrolCounter < targets.size()) {
//            currentTarget = targets.get(patrolCounter);
//        }
//
//        if (currentTarget == null) {
//            return;
//        }
//
//        // calc distance to current Target
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(currentTarget);
//        double distanceToTarget = dist.length();
//
//        if (distanceToTarget < 0.1d) {
//            patrolCounter++;
//            if (!(patrolCounter < targets.size()))
//                patrolCounter = 0;
//            ai.patrolCounter = patrolCounter;
//            entity.saveComponent(ai);
//            return;
//        }
//
//        setMovement(currentTarget, entity);
//    }
//
//    private void executeTestAI(EntityRef entity) {
//        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
//        if (localPlayer == null) {
//            return;
//        }
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//
//        if (!ai.locked) {
//            // get targets, break if none
//            List<Vector3f> targets = ai.movementTargets;
//            List<Vector3f> pathTargets = ai.pathTargets;
//            if ((targets == null) || (targets.size() < 1)) {
//                return;
//            }
//
//            Vector3f currentTarget; // check if currentTarget target is a path
//                                    // or not
//            if ((pathTargets != null) && (pathTargets.size() > 0)) {
//                currentTarget = pathTargets.get(0);
//            } else {
//                currentTarget = targets.get(0);
//            }
//            if (ai.previousTarget != ai.movementTargets.get(0)) {
//                ai.locked = true;
//                ai.pathTargets = aStarPathing.findPath(worldPos, new Vector3f(
//                        currentTarget));
//                if (ai.pathTargets == null) {
//                    // MinionSystem minionSystem = new MinionSystem();
//                    MinionMessage messagetosend = new MinionMessage(
//                            MinionMessagePriority.Debug, "test", "testdesc",
//                            "testcont", entity, localPlayer.getCharacterEntity());
//                    entity.send(new MinionMessageEvent(messagetosend));
//                    ai.movementTargets.remove(0);
//                }
//            }
//            ai.locked = false;
//            if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
//                pathTargets = ai.pathTargets;
//                ai.previousTarget = targets.get(0); // used to check if the
//                                                    // final target changed
//                currentTarget = pathTargets.get(0);
//            }
//
//            // trying to solve distance calculation with some simple trick of
//            // reducing the height to a round int, might not work for taller
//            // entities
//            worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;
//            // calc distance to current Target
//            Vector3f dist = new Vector3f(worldPos);
//            dist.sub(currentTarget);
//            double distanceToTarget = dist.length();
//
//            if (distanceToTarget < 0.1d) {
//                if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
//                    ai.pathTargets.remove(0);
//                    entity.saveComponent(ai);
//                } else {
//                    if (ai.movementTargets.size() > 0) {
//                        ai.movementTargets.remove(0);
//                    }
//                    ai.previousTarget = null;
//                    entity.saveComponent(ai);
//                }
//                return;
//            }
//
//            setMovement(currentTarget, entity);
//        }
//    }
//
//    private void setMovement(Vector3f currentTarget, EntityRef entity) {
//        LocationComponent location = entity
//                .getComponent(LocationComponent.class);
//        SimpleMinionAIComponent ai = entity
//                .getComponent(SimpleMinionAIComponent.class);
//        NPCMovementInputComponent movementInput = entity
//                .getComponent(NPCMovementInputComponent.class);
//        AnimationComponent animcomp = entity
//                .getComponent(AnimationComponent.class);
//        SkeletalMeshComponent skeletalcomp = entity
//                .getComponent(SkeletalMeshComponent.class);
//        Vector3f worldPos = new Vector3f(location.getWorldPosition());
//
//        Vector3f dist = new Vector3f(worldPos);
//        dist.sub(currentTarget);
//        double distanceToTarget = dist.length();
//
//        Vector3f targetDirection = new Vector3f();
//        targetDirection.sub(currentTarget, worldPos);
//        if (targetDirection.x * targetDirection.x + targetDirection.z
//            * targetDirection.z > 0.03f) {
//            if (timer.getGameTimeInMs() - ai.lastDistancecheck > 2000) {
//                ai.lastDistancecheck = timer.getGameTimeInMs();
//                if (ai.lastPosition == null) {
//                    ai.lastPosition = location.getWorldPosition();
//                } else if (ai.lastPosition.x == location.getLocalPosition().x
//                           && ai.lastPosition.z == location.getWorldPosition().z) {
//                    // minion has been stuck at same position => teleport
//                    if (skeletalcomp.animation == animcomp.walkAnim) {
//                        changeAnimation(entity, animcomp.idleAnim, true);
//                    }
//                    location.setWorldPosition(currentTarget);
//                    movementInput.directionToMove = new Vector3f(0, 0, 0);
//                    entity.saveComponent(location);
//                    entity.saveComponent(movementInput);
//                    ai.lastPosition = location.getWorldPosition();
//                } else {
//                    ai.lastPosition = location.getWorldPosition();
//                }
//            }
//            changeAnimation(entity, animcomp.walkAnim, true);
//            targetDirection.normalize();
//            movementInput.directionToMove = targetDirection;
//
//            float yaw = (float) Math
//                    .atan2(targetDirection.x, targetDirection.z);
//            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
//            location.getLocalRotation().set(axisAngle);
//        } else if (distanceToTarget > 1) {
//            // the minion arrived at right x and z but is standing below / above
//            // it => teleport
//            location.setWorldPosition(currentTarget);
//        } else {
//            if (skeletalcomp.animation == animcomp.walkAnim) {
//                changeAnimation(entity, animcomp.idleAnim, true);
//            }
//            movementInput.directionToMove = new Vector3f(0,0,0);
//        }
//        entity.saveComponent(ai);
//        entity.saveComponent(movementInput);
//        entity.saveComponent(location);
//    }
//
//    private boolean attack(EntityRef minion, Vector3f position) {
//
//        int damage = 1;
//        Block block = worldProvider.getBlock(new Vector3f(position.x,
//                position.y - 0.5f, position.z));
//        if ((block.isDestructible()) && (block.isTargetable())) {
//            EntityRef blockEntity = blockEntityRegistry
//                    // originally was getOrCreateEntityAt
//                    .getEntityAt(new Vector3i(position));
//            if (blockEntity == EntityRef.NULL) {
//                return false;
//            } else {
//                blockEntity.send(new DoDamageEvent(damage, EngineDamageTypes.DIRECT.get(), minion));
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
//    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
//        NPCMovementInputComponent movementInput = entity
//                .getComponent(NPCMovementInputComponent.class);
//        CharacterMovementComponent chracterMovement = entity
//                .getComponent(CharacterMovementComponent.class);
//        if ((movementInput != null) && (chracterMovement.grounded)) {
//            movementInput.jumpingRequested = true;
//            entity.saveComponent(movementInput);
//        }
//    }
//
//    @Override
//    public void preBegin() {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void postBegin() {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void preSave() {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void postSave() {
//        // TODO Auto-generated method stub
//        
//    }
//}
