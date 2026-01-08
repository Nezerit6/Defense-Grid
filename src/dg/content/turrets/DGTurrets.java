package dg.content.turrets;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.math.Interp;
import mindustry.content.*;
import mindustry.entities.abilities.MoveEffectAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Weapon;
import mindustry.type.unit.MissileUnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.BuildVisibility;

import static mindustry.type.ItemStack.with;

public class DGTurrets {
    public static Block

            arclet, needler, shatter, cryolance, arcflash;

    public static void load(){

        arclet = new PowerTurret("arclet"){{
            requirements(Category.turret, with(
                    Items.copper, 60,
                    Items.lead, 60,
                    Items.silicon, 40
            ));
            size = 1;
            health = 280;
            range = 95f;
            reload = 30f;
            recoil = 1f;
            shootCone = 40f;
            rotateSpeed = 8f;
            targetAir = false;
            heatColor = Color.red;
            shootSound = Sounds.spark;
            shootEffect = Fx.lightningShoot;
            consumePower(3.8f);
            coolant = consumeCoolant(0.1f);

            shootType = new LightningBulletType(){{
                damage = 23;
                lightningLength = 13;
                collidesAir = false;
                ammoMultiplier = 1f;
                buildingDamageMultiplier = 0.25f;

                lightningType = new BulletType(0.0001f, 0f){{
                    lifetime = Fx.lightning.lifetime;
                    hitEffect = Fx.hitLancer;
                    despawnEffect = Fx.none;
                    status = StatusEffects.shocked;
                    statusDuration = 10f;
                    hittable = false;
                    lightColor = Color.white;
                    collidesAir = false;
                    buildingDamageMultiplier = 0.25f;
                }};
            }};
        }};
        // todo rework
        needler = new ItemTurret("needler"){{
            requirements(Category.turret, BuildVisibility.hidden, with(
                    Items.copper, 55,
                    Items.graphite, 45,
                    Items.lead, 30
            ));
            size = 1;
            health = 200;
            range = 140;
            reload = 5f;
            recoil = 0.3f;
            inaccuracy = 4f;
            rotateSpeed = 10;
            ammoUseEffect = Fx.casing1;
            shootSound = Sounds.shoot;
            coolant = consumeCoolant(0.1f);

            ammo(
                    Items.copper, new BasicBulletType(12f, 9){{
                        width = 5f;
                        height = 7f;
                        lifetime = 11.7f;
                        ammoMultiplier = 2;
                    }},
                    Items.graphite, new BasicBulletType(14f, 14){{
                        width = 6f;
                        height = 8f;
                        lifetime = 10f;
                        ammoMultiplier = 3;
                        reloadMultiplier = 0.75f;
                    }},
                    Items.pyratite, new BasicBulletType(11f, 12){{
                        width = 6f;
                        height = 8f;
                        lifetime = 12.7f;
                        frontColor = Pal.lightishOrange;
                        backColor = Pal.lightOrange;
                        status = StatusEffects.burning;
                        ammoMultiplier = 4;
                        makeFire = true;
                    }}
            );
        }};

        shatter = new ItemTurret("shatter"){{
            requirements(Category.turret, with(
                    Items.copper, 120,
                    Items.graphite, 90,
                    Items.titanium, 60
            ));
            size = 2;
            health = 720;
            range = 250;
            reload = 75f;
            recoil = 2.5f;
            rotateSpeed = 2f;
            inaccuracy = 2f;
            shootCone = 12f;
            ammoPerShot = 2;
            targetAir = false;
            shootSound = Sounds.artillery;
            ammoUseEffect = Fx.casing2;
            coolant = consumeCoolant(0.2f);

            ammo(
                    Items.graphite, new ArtilleryBulletType(3.2f, 35){{
                        lifetime = 78f;
                        width = height = 12f;
                        splashDamage = 45f;
                        splashDamageRadius = 26f;
                        knockback = 0.8f;
                        collidesTiles = false;

                        hitColor = backColor = trailColor = Color.valueOf("ea8878");
                        trailLength = 12;
                        trailWidth = 2f;
                        trailSinScl = 2.5f;
                        trailSinMag = 0.5f;
                        trailEffect = Fx.none;

                        fragBullets = 6;
                        fragVelocityMin = 0.8f;
                        fragRandomSpread = 360;
                        fragLifeMin = 0.9f;
                        fragBullet = new BasicBulletType(4f, 15){{
                            lifetime = 12f;
                            width = 6f;
                            height = 5f;
                            pierceBuilding = true;
                            pierceCap = 2;
                        }};
                    }},

                    Items.silicon, new ArtilleryBulletType(3.2f, 35){{
                        lifetime = 78f;
                        width = height = 12f;
                        splashDamage = 45f;
                        splashDamageRadius = 26f;
                        knockback = 0.8f;
                        collidesTiles = false;
                        homingPower = 0.08f;
                        homingRange = 60f;
                        reloadMultiplier = 1.15f;
                        ammoMultiplier = 3f;

                        hitColor = backColor = trailColor = Color.valueOf("ea8878");
                        trailLength = 12;
                        trailWidth = 2f;
                        trailSinScl = 2.5f;
                        trailSinMag = 0.5f;
                        trailEffect = Fx.none;

                        fragBullets = 6;
                        fragVelocityMin = 0.8f;
                        fragRandomSpread = 360;
                        fragLifeMin = 0.9f;
                        fragBullet = new BasicBulletType(4f, 15){{
                            lifetime = 12f;
                            width = 6f;
                            height = 5f;
                            pierceBuilding = true;
                            pierceCap = 2;
                        }};
                    }},

                    Items.pyratite, new ArtilleryBulletType(3f, 42){{
                        lifetime = 83f;
                        width = height = 13f;
                        splashDamage = 55f;
                        splashDamageRadius = 30f;
                        knockback = 0.9f;
                        collidesTiles = false;
                        status = StatusEffects.burning;
                        statusDuration = 60f * 10f;
                        makeFire = true;
                        ammoMultiplier = 3f;

                        frontColor = Pal.lightishOrange;
                        backColor = trailColor = Pal.lightOrange;
                        trailLength = 12;
                        trailWidth = 2f;
                        trailSinScl = 2.5f;
                        trailSinMag = 0.5f;
                        trailEffect = Fx.incendTrail;

                        fragBullets = 7;
                        fragVelocityMin = 0.8f;
                        fragRandomSpread = 360;
                        fragLifeMin = 0.9f;
                        fragBullet = new BasicBulletType(3.5f, 18){{
                            lifetime = 14f;
                            width = 6f;
                            height = 5f;
                            pierceBuilding = true;
                            pierceCap = 2;
                            status = StatusEffects.burning;
                        }};
                    }}
            );

            drawer = new DrawTurret(){{
                parts.add(
                        new RegionPart("-barrel"){{
                            progress = PartProgress.recoil.curve(Interp.pow2In);
                            moveY = -2f;
                            heatColor = Color.valueOf("f03b0e");
                            mirror = false;
                        }},
                        new RegionPart("-front"){{
                            heatProgress = PartProgress.warmup;
                            progress = PartProgress.warmup;
                            mirror = true;
                            moveX = -1f;
                            under = true;
                        }}
                );
            }};
        }};

        cryolance = new LiquidTurret("cryolance"){{
            requirements(Category.turret, with(
                    Items.metaglass, 90,
                    Items.lead, 140,
                    Items.titanium, 100,
                    Items.silicon, 80
            ));
            size = 2;
            health = 430;
            range = 205;
            reload = 120f;
            recoil = 3f;
            shootY = 6.5f;
            rotateSpeed = 4.2f;
            inaccuracy = 5;
            liquidCapacity = 60;
            targetAir = false;
            extinguish = false;
            moveWhileCharging = false;
            accurateDelay = false;
            heatColor = Color.valueOf("afeeee");
            shootSound = Sounds.malignShoot;
            loopSound = Sounds.none;
            shootEffect = Fx.none;
            smokeEffect = Fx.hitLancer;
            shoot.firstShotDelay = 60f;
            consumePower(3.6f);

            ammo(
                    Liquids.cryofluid, new BasicBulletType(12.6f, 36){{
                        lifetime = 17.5f;
                        width = 4;
                        height = 28;
                        hitColor = backColor = trailColor = Color.valueOf("afeeee");
                        trailLength = 3;
                        trailWidth = 1.9f;
                        homingPower = 0.03f;
                        homingDelay = 2f;
                        homingRange = 60f;
                        ammoMultiplier = 0.2f;
                        collidesAir = false;
                        hitEffect = Fx.none;
                        chargeEffect = new MultiEffect(Fx.lancerLaserCharge, Fx.lancerLaserChargeBegin);
                        fragBullets = 1;
                    }}
            );

            drawer = new DrawTurret(){{
                parts.add(
                        new RegionPart("-nozzle"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.charge;
                            mirror = true;
                            moveRot = 7f;
                            heatColor = Color.valueOf("afeeee");
                            moves.add(new PartMove(PartProgress.recoil, 0f, 0f, -30f));
                        }}
                );
            }};
        }};

        // todo maybe need balance
        arcflash = new ItemTurret("arcflash"){{
            requirements(Category.turret, with(
                    Items.copper, 150,
                    Items.graphite, 120,
                    Items.titanium, 80,
                    Items.plastanium, 60
            ));

            ammo(
                    Items.blastCompound, new MissileBulletType(2.5f, 18){{
                        ammoMultiplier = 3f;

                        spawnUnit = new MissileUnitType("arcflash-missile"){{
                            outlineColor = Pal.darkerMetal;
                            speed = 4.2f;
                            maxRange = 6f;
                            lifetime = 60f * 2f;
                            engineColor = trailColor = Pal.missileYellowBack;
                            engineLayer = Layer.effect;
                            engineSize = 1.5f;
                            engineOffset = 4f;
                            trailLength = 8;
                            health = 50;
                            lowAltitude = true;
                            loopSound = Sounds.missileTrail;
                            loopSoundVolume = 0.1f;
                            deathSound = Sounds.explosion;
                            targetAir = false;
                            homingDelay = 8f;

                            fogRadius = 3f;

                            weapons.add(new Weapon(){{
                                shootCone = 360f;
                                mirror = false;
                                reload = 1f;
                                shootOnDeath = true;
                                bullet = new ExplosionBulletType(90f, 30f){{
                                    collidesAir = false;
                                    shootEffect = new MultiEffect(Fx.blastExplosion, new WaveEffect(){{
                                        colorFrom = colorTo = Pal.missileYellow;
                                        sizeTo = 30f;
                                        lifetime = 10f;
                                        strokeFrom = 3f;
                                    }});
                                    buildingDamageMultiplier = 0.35f;

                                    status = StatusEffects.blasted;
                                    statusDuration = 60f;
                                }};
                            }});
                        }};
                    }}
            );

            drawer = new DrawTurret(){{
                parts.add(
                        new RegionPart("-side"){{
                            progress = PartProgress.warmup;
                            mirror = true;

                            moveX = -0.5f;
                            moveY = 0f;

                            moves.add(new PartMove(PartProgress.recoil, 1.2f, 0f, -15f));
                        }},
                        new RegionPart("-missile"){{
                            progress = PartProgress.reload.curve(Interp.pow2In);

                            color = Color.white;
                            colorTo = new Color(1f, 1f, 1f, 0f);
                            mixColorTo = Pal.accent;
                            mixColor = new Color(1f, 1f, 1f, 0f);
                            outline = false;

                            under = true;
                            layerOffset = -0.01f;

                            mirror = false;

                            moves.add(new PartMove(PartProgress.warmup.inv(), 0f, -4f, 0f));
                        }}
                );
            }};

            size = 2;
            scaledHealth = 220;
            range = 440f;
            reload = 220f;
            minWarmup = 0.95f;
            shootWarmupSpeed = 0.05f;
            targetAir = false;
            targetUnderBlocks = false;
            ammoPerShot = 2;
            maxAmmo = 20;
            shootY = 2f;
            recoil = 0.5f;
            shake = 3.5f;
            shootCone = 8f;
            rotateSpeed = 2.5f;

            shootSound = Sounds.mediumCannon;
            coolant = consumeCoolant(0.2f);

            limitRange();
        }};
    }
}