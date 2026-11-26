package com.rizhik.visuals.client.module;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoCart extends Module {
    public static KeyMapping key;

    private enum Step { IDLE, PLACE, BOW_START, BOW_CHARGE, REFILL }

    private final Setting<Double> charge = add(new Setting<Double>("Заряд лука (тики)", 4.0, 3, 20));
    private final Setting<Double> delay = add(new Setting<Double>("Пауза между шагами", 1.0, 0, 5));
    private final Setting<Boolean> autoRail = add(new Setting<Boolean>("Ставить рельс сам", true));
    private final Setting<Boolean> refill = add(new Setting<Boolean>("Новая вагонетка", true));

    private Step step = Step.IDLE;
    private int wait;
    private int orig;
    private int cartSlot;
    private BlockHitResult railHit;
    private boolean forcedUse;

    public AutoCart() {
        super("AutoCart", Category.PVP,
                "По клавише ставит TNT-вагонетку на рельс под прицелом, быстро стреляет в неё из лука (нужен Flame), берёт новую вагонетку из инвентаря в тот же слот и возвращает твой слот. Клавишу выбери в настройках управления.");
    }

    @Override
    protected void onEnable() {
        if (key != null) while (key.consumeClick()) { }
    }

    @Override
    protected void onDisable() {
        abort(Minecraft.getInstance());
    }

    private static int ticks(Setting<Double> s) {
        return (int) Math.round(s.get());
    }

    private void swap(Minecraft mc, int invSlot, int hotbar) {
        mc.gameMode.handleInventoryMouseClick(
                mc.player.inventoryMenu.containerId, invSlot, hotbar, ClickType.SWAP, mc.player);
    }

    private int find(Inventory inv, Item item) {
        for (int i = 0; i < 36; i++) if (inv.getItem(i).is(item)) return i;
        return -1;
    }

    private int findInInventory(Inventory inv, Item item) {
        for (int i = 9; i < 36; i++) if (inv.getItem(i).is(item)) return i;
        return -1;
    }

    private int findHotbarTag(Inventory inv, TagKey<Item> tag) {
        for (int i = 0; i < 9; i++) if (inv.getItem(i).is(tag)) return i;
        return -1;
    }

    private boolean hasArrow(Minecraft mc) {
        if (mc.player.getAbilities().instabuild) return true;
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) if (inv.getItem(i).is(ItemTags.ARROWS)) return true;
        return mc.player.getOffhandItem().is(ItemTags.ARROWS);
    }

    private void abort(Minecraft mc) {
        if (forcedUse && mc != null) {
            mc.options.keyUse.setDown(false);
            if (mc.player != null && mc.gameMode != null) mc.gameMode.releaseUsingItem(mc.player);
        }
        forcedUse = false;
        if (step != Step.IDLE && mc != null && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(orig);
        }
        step = Step.IDLE;
        railHit = null;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (key == null) return;
        boolean pressed = false;
        while (key.consumeClick()) pressed = true;

        if (mc.player == null || mc.gameMode == null || mc.level == null) {
            if (step != Step.IDLE) abort(mc);
            return;
        }
        if (step == Step.IDLE) {
            if (pressed && mc.screen == null) start(mc);
            return;
        }
        if (mc.screen != null) {
            abort(mc);
            return;
        }
        if (wait > 0) {
            wait--;
            return;
        }

        var inv = mc.player.getInventory();
        switch (step) {
            case PLACE -> {
                inv.setSelectedSlot(cartSlot);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, railHit);
                mc.player.swing(InteractionHand.MAIN_HAND);
                step = Step.BOW_START;
                wait = ticks(delay);
            }
            case BOW_START -> {
                int bow = -1;
                for (int i = 0; i < 9; i++) {
                    if (inv.getItem(i).is(Items.BOW)) { bow = i; break; }
                }
                if (bow < 0) {
                    int bi = findInInventory(inv, Items.BOW);
                    if (bi < 0) { abort(mc); return; }
                    swap(mc, bi, cartSlot);
                    bow = cartSlot;
                }
                inv.setSelectedSlot(bow);
                mc.options.keyUse.setDown(true);
                forcedUse = true;
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                step = Step.BOW_CHARGE;
                wait = Math.max(0, ticks(charge) - 1);
            }
            case BOW_CHARGE -> {
                mc.gameMode.releaseUsingItem(mc.player);
                mc.options.keyUse.setDown(false);
                forcedUse = false;
                step = Step.REFILL;
                wait = ticks(delay);
            }
            case REFILL -> {
                if (refill.get()) {
                    int next = findInInventory(inv, Items.TNT_MINECART);
                    if (next >= 0) swap(mc, next, cartSlot);
                }
                inv.setSelectedSlot(orig);
                step = Step.IDLE;
                railHit = null;
            }
            default -> step = Step.IDLE;
        }
    }

    private void start(Minecraft mc) {
        var inv = mc.player.getInventory();
        orig = inv.getSelectedSlot();

        int cart = find(inv, Items.TNT_MINECART);
        if (cart < 0 || find(inv, Items.BOW) < 0 || !hasArrow(mc)) return;

        BlockHitResult target = null;
        boolean railPlaced = false;
        if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            BlockPos bp = bhr.getBlockPos();
            if (mc.level.getBlockState(bp).is(BlockTags.RAILS)) {
                target = bhr;
            } else if (autoRail.get()) {
                int rail = findHotbarTag(inv, ItemTags.RAILS);
                if (rail >= 0) {
                    inv.setSelectedSlot(rail);
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, bhr);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    BlockPos np = bp.relative(bhr.getDirection());
                    target = new BlockHitResult(
                            Vec3.atBottomCenterOf(np).add(0, 0.0625, 0), Direction.UP, np, false);
                    railPlaced = true;
                }
            }
        }
        if (target == null) {
            inv.setSelectedSlot(orig);
            return;
        }

        if (cart >= 9) {
            swap(mc, cart, orig);
            cartSlot = orig;
        } else {
            cartSlot = cart;
        }
        railHit = target;
        step = Step.PLACE;
        wait = railPlaced ? Math.max(1, ticks(delay)) : 0;
    }
}
