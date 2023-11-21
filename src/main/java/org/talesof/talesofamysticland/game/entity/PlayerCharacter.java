package org.talesof.talesofamysticland.game.entity;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.talesof.talesofamysticland.game.main.GamePanel;
import org.talesof.talesofamysticland.game.main.KeyHandler;

public abstract class PlayerCharacter extends Entity {

    KeyHandler keyH;

    public final int screenX;
    public final int screenY;
    public boolean attackCanceled = false;
    public boolean lightUpdated = false;

    public int spriteNumVertical = 1;
    public int spriteNumHorizontal = 1;

    public BufferedImage 
    up1, up2, up3, up4, up5,
    down1, down2, down3, down4, down5,
    left1, left2, left3,
    right1, right2, right3;

    public BufferedImage 
    attackUp1, attackUp2, attackUp3,
    attackDown1, attackDown2, attackDown3,
    attackLeft1, attackLeft2, attackLeft3, attackLeft4,
    attackRight1, attackRight2, attackRight3, attackRight4;

    public PlayerCharacter(GamePanel gp) {

        super(gp);

        type = typePlayer;

        screenX = gp.screenWidth/2 - (gp.tileSize / 2);
        screenY = gp.screenHeight/2 - (gp.tileSize / 2);

        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        setDefaultValues();
    }

    public GamePanel getGamePanel() {
        return gp;
    }

    public void setGamePanel(GamePanel gp) {
        this.gp = gp;
    }  

    public void setKeyHandler(KeyHandler keyH) {
        this.keyH = keyH;
    }

    public void setDefaultValues() {

        setDefaultPositions();
        defaultSpeed = 4;
        speed = defaultSpeed;

        // Player Status
        level = 1;

        maxLife = 6;
        life = maxLife;
        maxMana = 4;
        mana = maxMana;

        strength = 1;
        dexterity = 1;
        exp = 0;
        nextLevelExp = 5;
        coin = 500;

        currentLight = null;

        getImage();
        getAttackImage();
        setItems();
        setDialogue();
    }

    public void setDefaultPositions() {
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        gp.currentMap = 0;
        direction = "down";
    }

    public void setDialogue() {
        dialogues[0][0] = "Você subiu para o level " + level + "!\n"
                + "Você se sente mais forte!";
    }

    public int getAttack() {
        attackArea = currentWeapon.attackArea;
        motion1Duration = currentWeapon.motion1Duration;
        motion2Duration = currentWeapon.motion2Duration;
        return strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return dexterity * currentShield.defenseValue;
    }

    public int getCurrentWeaponSlot() {
        int currentWeaponSlot = -1;
        for(int i = 0; i < inventory.size(); i++) {
            if(inventory.get(i) == currentWeapon) {
                currentWeaponSlot = i;
            }
        }
        return currentWeaponSlot;
    }

    public int getCurrentShieldSlot() {
        int currentShieldSlot = -1;
        for(int i = 0; i < inventory.size(); i++) {
            if(inventory.get(i) == currentShield) {
                currentShieldSlot = i;
            }
        }
        return currentShieldSlot;
    }

    public abstract void setItems();

    public void restoreStatus() {
        life = maxLife;
        mana = maxMana;
        speed = defaultSpeed;
        invincible = false;
        transparent = false;
        attacking = false;
        guarding = false;
        knockBack = false;
        lightUpdated = true;
    }

    public abstract void getImage();

    public abstract void getAttackImage();

    public void getSleepingImage(BufferedImage image) {
        up1 = image;
        up2 = image;
        down1 = image;
        down2 = image;
        left1 = image;
        left2 = image;
        right1 = image;
        right2 = image;
    }

    public void update() {

        if(knockBack) {

            collisionOn = false;
            gp.cChecker.checkTile(this);
            gp.cChecker.checkEntity(this, gp.iTile);
            gp.cChecker.checkObject(this, true);
            gp.cChecker.checkEntity(this, gp.npc);
            gp.cChecker.checkEntity(this, gp.monster);

            if(collisionOn) {
                knockBackCounter = 0;
                knockBack = false;
                speed = defaultSpeed;

            } else {
                switch(knockBackDirection) {
                    case "up" -> worldY -= speed;
                    case "down" -> worldY += speed;
                    case "left" -> worldX -= speed;
                    case "right" -> worldX += speed;
                }
            }

            knockBackCounter++;
            if(knockBackCounter == 10) {
                knockBackCounter = 0;
                knockBack = false;
                speed = defaultSpeed;
            }
        }
        else if (attacking) {
            attacking();
        }
        else if(keyH.spacePressed) {
            guarding = true;
            guardCounter++;
        }
        else if (keyH.upPressed || keyH.downPressed
                || keyH.leftPressed || keyH.rightPressed
                || keyH.enterPressed) {

            if (keyH.upPressed)
                direction = "up";

            if (keyH.downPressed)
                direction = "down";

            if (keyH.leftPressed)
                direction = "left";

            if (keyH.rightPressed)
                direction = "right";

            // Check tile collision
            collisionOn = false;
            gp.cChecker.checkTile(this);
            gp.cChecker.checkEntity(this, gp.iTile);

            // Check object collision
            int objIndex = gp.cChecker.checkObject(this, true);
            interactObject(objIndex);
            pickUpObject(objIndex);

            // Check npc collision
            int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
            interactNPC(npcIndex);

            // Check monster collision
            int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
            contactMonster(monsterIndex);

            // Check event
            gp.eHandler.checkEvent();

            // if collision is false, player can move
            if (!collisionOn && !keyH.enterPressed) {
                switch (direction) {
                    case "up" -> worldY -= speed;
                    case "down" -> worldY += speed;
                    case "left" -> worldX -= speed;
                    case "right" -> worldX += speed;
                }
            }

            if (keyH.enterPressed && !attackCanceled) {
                gp.playSoundEffect(7);
                attacking = true;
                spriteCounter = 0;
            }

            attackCanceled = false;
            gp.keyH.enterPressed = false;
            guarding = false;
            guardCounter = 0;

            spriteCounter++;

            if (spriteCounter > 12) {
                switch(direction) {
                    case "up", "down" -> {
                        if (spriteNumVertical == 1) {
                            spriteNumVertical = 2;
                        } else if (spriteNumVertical == 2) {
                            spriteNumVertical = 3;
                        } else if (spriteNumVertical == 3) {
                            spriteNumVertical = 4;
                        } else if (spriteNumVertical == 4) {
                            spriteNumVertical = 5;
                        } else if (spriteNumVertical == 5) {
                            spriteNumVertical = 1;
                        }
                    }
                    
                    case "left", "right" -> {
                        if (spriteNumHorizontal == 1) {
                            spriteNumHorizontal = 2;
                        } else if (spriteNumHorizontal == 2) {
                            spriteNumHorizontal = 3;
                        } else if (spriteNumHorizontal == 3) {
                            spriteNumHorizontal = 1;
                        }
                    }
                }

                spriteCounter = 0;
            }
        }
        else {
            standCounter++;
            if(standCounter == 40) {
                spriteNum = 1;
                standCounter = 0;
            }
            guarding = false;
            guardCounter = 0;
        }

        if (gp.keyH.shotKeyPressed && !projectile.alive
                && shotAvailableCounter == 30 && projectile.haveResource(this)) {

            // Set default coordinates, direction and user
            projectile.set(worldX, worldY, direction, true, this);

            // Subtract the cost (Mana, AMMO)
            projectile.subtractResource(this);

            for (int i = 0; i < gp.projectile[1].length; i++) {
                if (gp.projectile[gp.currentMap][i] == null) {
                    gp.projectile[gp.currentMap][i] = projectile;
                    break;
                }
            }

            shotAvailableCounter = 0;

            gp.playSoundEffect(10);
        }

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincible = false;
                transparent = false;
                invincibleCounter = 0;
            }
        }

        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }

        if (life > maxLife) {
            life = maxLife;
        }

        if (mana > maxMana) {
            mana = maxMana;
        }

        if (life <= 0) {
            gp.gameState = gp.gameOverState;
            gp.player.attackCanceled = true;
            gp.player.invincible = false;
            gp.player.transparent = false;
            gp.ui.commandNum = -1;
            gp.stopMusic();
            gp.playSoundEffect(12);
        }
    }

    public boolean inventoryIsNotFull() {
        return inventory.size() != maxInventorySize;
    }

    public void pickUpObject(int i) {

        if(i != 999) {
            if(gp.obj[gp.currentMap][i].type != typeObstacle) {
                // Pickup only items
                if (gp.obj[gp.currentMap][i].type == typePickup) {

                    gp.obj[gp.currentMap][i].use(this);

                } else {

                    // Inventory items
                    String text;

                    if(canObtainItem(gp.obj[gp.currentMap][i])) {

                        gp.playSoundEffect(1);
                        text = "Got a " + gp.obj[gp.currentMap][i].name + "!";

                    } else {
                        text = "You cannot carry any more!";
                    }

                    gp.ui.addMessage(text);
                }

                gp.obj[gp.currentMap][i] = null;
            }
        }
    }

    public void interactObject(int i) {
        if(i != 999) {
            attackCanceled = true;
            if(gp.obj[gp.currentMap][i].type == typeObstacle) {
                if(keyH.enterPressed) {
                    gp.obj[gp.currentMap][i].interact();
                }
            }
        }
    }

    public void interactNPC(int i) {

        if(gp.keyH.enterPressed) {
            if(i != 999) {
                attackCanceled = true;
                gp.npc[gp.currentMap][i].speak();
            }
        }
    }

    private void contactMonster(int i) {

        if(i != 999) {

            if(!invincible && !gp.monster[gp.currentMap][i].dying) {
                gp.playSoundEffect(6);

                int damage = gp.monster[gp.currentMap][i].attack - defense;
                if(damage < 1) {
                    damage = 1;
                }

                life -= damage;
                invincible = true;
                transparent = true;
            }
        }
    }

    public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {

        if(i != 999) {

            if(!gp.monster[gp.currentMap][i].invincible) {
                gp.playSoundEffect(5);

                if(knockBackPower > 0) {
                    setKnockBack(gp.monster[gp.currentMap][i], attacker, knockBackPower);
                }

                if(gp.monster[gp.currentMap][i].offBalance) {
                    attack *= 5;
                }

                int damage = attack - gp.monster[gp.currentMap][i].defense;
                if(damage < 0) {
                    damage = 0;
                }

                gp.monster[gp.currentMap][i].life -= damage;
                if(gp.monster[gp.currentMap][i].life <= 0) {
                    gp.monster[gp.currentMap][i].life = 0;
                }
                gp.ui.addMessage(damage + " damage!");

                gp.monster[gp.currentMap][i].invincible = true;
                gp.monster[gp.currentMap][i].damageReaction();

                if(gp.monster[gp.currentMap][i].life <= 0) {
                    gp.monster[gp.currentMap][i].dying = true;
                    gp.ui.addMessage("Killed the " + gp.monster[gp.currentMap][i].name + "!");
                    gp.ui.addMessage("+ " + gp.monster[gp.currentMap][i].exp + " exp");
                    exp += gp.monster[gp.currentMap][i].exp;
                    checkLevelUp();
                }
            }
        }
    }

    public void damageInteractiveTile(int i) {

        if(i != 999 && gp.iTile[gp.currentMap][i].destructible
                && gp.iTile[gp.currentMap][i].itemIsCorrect(this) && !gp.iTile[gp.currentMap][i].invincible) {

            gp.iTile[gp.currentMap][i].playSE();
            gp.iTile[gp.currentMap][i].life--;
            gp.iTile[gp.currentMap][i].invincible = true;

            generateParticle(gp.iTile[gp.currentMap][i], gp.iTile[gp.currentMap][i]);

            if(gp.iTile[gp.currentMap][i].life == 0) {
                gp.iTile[gp.currentMap][i] = gp.iTile[gp.currentMap][i].getDestroyedForm();
            }
        }
    }

    public void damageProjectile(int i) {

        if(i != 999) {
            Entity projectile = gp.projectile[gp.currentMap][i];
            projectile.alive = false;
            generateParticle(projectile, projectile);
        }
    }

    public void checkLevelUp() {

        if(exp >= nextLevelExp) {

            level++;
            nextLevelExp *= 2;
            maxLife += 2;
            strength++;
            dexterity++;
            attack = getAttack();
            defense = getDefense();

            gp.playSoundEffect(8);
            gp.gameState = gp.dialogueState;

            setDialogue();
            startDialogue(this, 0);
        }
    }

    public void selectItem() {

        int itemIndex = gp.ui.getItemIndexOnSlot(gp.ui.playerSlotCol, gp.ui.playerSlotRow);

        if(itemIndex < inventory.size()) {

            Entity selectedItem = inventory.get(itemIndex);

            if(selectedItem.type == typeSword || selectedItem.type == typeAxe) {
                currentWeapon = selectedItem;
                attack = getAttack();
                getAttackImage();
            }

            if(selectedItem.type == typeShield) {
                currentShield = selectedItem;
                defense = getDefense();
            }

            if(selectedItem.type == typeLight) {
                if(currentLight == selectedItem) {
                    currentLight = null;
                }
                else {
                    currentLight = selectedItem;
                }
                lightUpdated = true;
            }

            if(selectedItem.type == typeConsumable) {
                if(selectedItem.use(this)) {
                    if(selectedItem.amount > 1) {
                        selectedItem.amount--;
                    } else {
                        inventory.remove(itemIndex);
                    }
                }
            }
        }
    }

    public int searchItemInInventory(String itemName) {

        int itemIndex = 999;

        for(int i = 0; i < inventory.size(); i++) {
            if(inventory.get(i).name.equals(itemName)) {
                itemIndex = i;
                break;
            }
        }

        return itemIndex;
    }

    public boolean canObtainItem(Entity item) {

        boolean canObtain = false;

        Entity newItem = gp.eGenerator.getObject(item.name);

        // Check if item is stackable
        if(newItem.stackable) {
            int index = searchItemInInventory(item.name);

            if(index != 999) {
                inventory.get(index).amount++;
                canObtain = true;
            }
            else {
                // New item
                if(inventoryIsNotFull()) {
                    inventory.add(newItem);
                    canObtain = true;
                }
            }
        } else {
            // Item not stackable
            if(inventoryIsNotFull()) {
                inventory.add(newItem);
                canObtain = true;
            }
        }

        return canObtain;
    }

    public void draw(Graphics2D g2) {

        BufferedImage image = null;
        int tempScreenX = screenX;
        int tempScreenY = screenY;

        switch (direction) {
            case "up" -> {
                if(!attacking) {
                    if (spriteNumVertical == 1) image = up1;
                    if (spriteNumVertical == 2) image = up2;
                    if (spriteNumVertical == 3) image = up3;
                    if (spriteNumVertical == 4) image = up4;
                    if (spriteNumVertical == 5) image = up5;
                }
                if(attacking) {
                    tempScreenY = screenY - gp.tileSize;
                    if (spriteNumVertical == 1) image = attackUp1;
                    if (spriteNumVertical == 2) image = attackUp2;
                    if (spriteNumVertical == 3) image = attackUp3;
                }
            }
            case "down" -> {
                if(!attacking) {
                    if (spriteNumVertical == 1) image = down1;
                    if (spriteNumVertical == 2) image = down2;
                    if (spriteNumVertical == 3) image = down3;
                    if (spriteNumVertical == 4) image = down4;
                    if (spriteNumVertical == 5) image = down5;
                }
                if(attacking) {
                    tempScreenY = screenY - gp.tileSize;
                    if (spriteNumVertical == 1) image = attackDown1;
                    if (spriteNumVertical == 2) image = attackDown2;
                    if (spriteNumVertical == 3) image = attackDown3;
                }
            }
            case "left" -> {
                if(!attacking) {
                    if (spriteNumHorizontal == 1) image = left1;
                    if (spriteNumHorizontal == 2) image = left2;
                    if (spriteNumHorizontal == 3) image = left3;
                }
                if(attacking) {
                    tempScreenY = screenY - gp.tileSize;
                    if (spriteNumHorizontal == 1) image = attackLeft1;
                    if (spriteNumHorizontal == 2) image = attackLeft2;
                    if (spriteNumHorizontal == 3) image = attackLeft3;
                    if (spriteNumHorizontal == 4) image = attackLeft4;
                }
            }
            case "right" -> {
                if(!attacking) {
                    if (spriteNumHorizontal == 1) image = right1;
                    if (spriteNumHorizontal == 2) image = right2;
                    if (spriteNumHorizontal == 3) image = right3;
                }
                if(attacking) {
                    tempScreenY = screenY - gp.tileSize;
                    if (spriteNumHorizontal == 1) image = attackRight1;
                    if (spriteNumHorizontal == 2) image = attackRight2;
                    if (spriteNumHorizontal == 3) image = attackRight3;
                    if (spriteNumHorizontal == 4) image = attackRight4;
                }
            }
        }

        // invincible opacity
        if(transparent) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }

        g2.drawImage(image, tempScreenX, tempScreenY, null);

        // reset opacity
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}