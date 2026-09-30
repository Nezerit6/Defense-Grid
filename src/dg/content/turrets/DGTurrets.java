package dg.content.turrets;

import arc.graphics.Color;
import arc.math.Interp;
import dg.content.DGFx;
import dg.entities.bullet.*;
import dg.world.blocks.SmokeTestBlock;
import dg.world.blocks.turrets.CapacitorTurret;
import dg.world.draw.DrawEmptyTurret;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.DrawPart.PartProgress;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootBarrel;
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

            arclet, needler, shatter, cryolance, arcflash,

            pulse, glaive, singularity, beacon, capacitor,

            smokeTest;

    public static void load(){

        arclet = new PowerTurret("arclet"){{
            requirements(Category.turret, with(
                    Items.copper, 60,
                    Items.lead, 60,
                    Items.silicon, 40
            ));
            size = 1;
            health = 280;
            range = 115f;
            reload = 30f;
            recoil = 1f;
            shootCone = 40f;
            rotateSpeed = 3.5f;
            targetAir = false;
            heatColor = Color.red;
            shootSound = Sounds.spark;
            shootEffect = Fx.lightningShoot;
            consumePower(4.3f);
            coolant = consumeCoolant(0.1f);

            shootType = new ChainLightningBulletType(){{
                damage = 23;
                lightningLength = 19;
                chainDamageFalloff = 0.15f;
                maxChains = 3;
                collidesAir = false;
                buildingDamageMultiplier = 0.35f;

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
                        smokeEffect = Fx.shootBigSmoke;

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
                                shootSound = Sounds.none;
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
            predictTarget = false;
            targetUnderBlocks = false;
            ammoPerShot = 2;
            maxAmmo = 20;
            shootY = 2f;
            recoil = 0.5f;
            shake = 3.5f;
            shootCone = 8f;
            rotateSpeed = 1.8f;

            shootSound = Sounds.mediumCannon;
            coolant = consumeCoolant(0.2f);

            limitRange();
        }};

        pulse = new PowerTurret("pulse"){{
            requirements(Category.turret, with(
                    Items.copper, 70,
                    Items.lead, 50,
                    Items.silicon, 45,
                    Items.graphite, 30
            ));
            size = 1;
            health = 300;
            range = 130f;
            reload = 55f;
            recoil = 1f;
            rotateSpeed = 6f;
            shootCone = 10f;
            heatColor = DGFx.pulseColor;
            shootSound = Sounds.lasershoot;
            shootEffect = DGFx.pulseShoot;
            smokeEffect = Fx.none;
            consumePower(2.5f);
            coolant = consumeCoolant(0.1f);

            shootType = new RicochetBulletType(4.5f, 16){{
                lifetime = 30f;
                width = 7f;
                height = 11f;
                bounces = 6;
                bounceRange = 90f;
                damageScale = 1.15f;
                speedScale = 1.08f;
                frontColor = Color.white;
                backColor = hitColor = trailColor = DGFx.pulseColor;
                trailLength = 7;
                trailWidth = 1.8f;
                hitEffect = DGFx.ricochet;
                despawnEffect = DGFx.pulseShoot;
                buildingDamageMultiplier = 0.3f;
            }};
        }};

        glaive = new ItemTurret("glaive"){{
            requirements(Category.turret, with(
                    Items.copper, 110,
                    Items.graphite, 80,
                    Items.titanium, 70
            ));
            size = 2;
            scaledHealth = 220;
            range = 125f;
            reload = 70f;
            recoil = 2f;
            rotateSpeed = 5f;
            shootCone = 8f;
            targetAir = false;
            shootSound = Sounds.swish;
            shootEffect = DGFx.glaiveThrow;
            smokeEffect = Fx.none;
            drawer = new DrawEmptyTurret();
            coolant = consumeCoolant(0.2f);

            ammo(
                    Items.titanium, new GlaiveBulletType(4.2f, 32){{
                        lifetime = 90f;
                        hitSize = 9f;
                        backColor = hitColor = trailColor = Color.valueOf("8da1e3");
                        frontColor = Color.white;
                        trailLength = 6;
                        trailWidth = 2f;
                        hitEffect = DGFx.glaiveHit;
                        buildingDamageMultiplier = 0.4f;
                    }},
                    Items.plastanium, new GlaiveBulletType(4.2f, 40){{
                        lifetime = 90f;
                        hitSize = 11f;
                        bladeLength = 8.5f;
                        blades = 4;
                        catchReload = 0.8f;
                        ammoMultiplier = 2f;
                        backColor = hitColor = trailColor = Pal.plastaniumBack;
                        frontColor = Pal.plastaniumFront;
                        trailLength = 6;
                        trailWidth = 2.2f;
                        hitEffect = DGFx.glaiveHit;
                        buildingDamageMultiplier = 0.4f;
                    }},
                    Items.thorium, new GlaiveBulletType(3.6f, 60){{
                        lifetime = 105f;
                        hitSize = 10f;
                        reloadMultiplier = 0.75f;
                        knockback = 2f;
                        backColor = hitColor = trailColor = Color.valueOf("f9a3c7");
                        frontColor = Color.white;
                        trailLength = 6;
                        trailWidth = 2f;
                        hitEffect = DGFx.glaiveHit;
                        buildingDamageMultiplier = 0.4f;
                    }}
            );
        }};

        singularity = new PowerTurret("singularity"){{
            requirements(Category.turret, with(
                    Items.lead, 150,
                    Items.silicon, 130,
                    Items.titanium, 100,
                    Items.thorium, 80,
                    Items.surgeAlloy, 40
            ));
            size = 2;
            scaledHealth = 240;
            range = 130f;
            reload = 240f;
            recoil = 0f;
            rotateSpeed = 2f;
            shootCone = 10f;
            targetAir = false;
            moveWhileCharging = false;
            accurateDelay = false;
            shoot.firstShotDelay = 50f;
            chargeSound = Sounds.lasercharge2;
            shootSound = Sounds.plasmaboom;
            shootEffect = Fx.none;
            smokeEffect = Fx.none;
            drawer = new DrawEmptyTurret();
            consumePower(9f);
            coolant = consumeCoolant(0.3f);

            shootType = new GravityWellBulletType(3.8f, 0f){{
                lifetime = 200f;
                drag = 0.03f;
                splashDamage = 180f;
                splashDamageRadius = 56f;
                tickDamage = 6f;
                pullRadius = 80f;
                pullForce = 0.6f;
                hitShake = 5f;
                hitSound = Sounds.plasmaboom;
                chargeEffect = DGFx.singularityCharge;
                buildingDamageMultiplier = 0.25f;
            }};
        }};

        beacon = new ItemTurret("beacon"){{
            requirements(Category.turret, with(
                    Items.copper, 200,
                    Items.graphite, 150,
                    Items.silicon, 120,
                    Items.titanium, 100,
                    Items.plastanium, 60
            ));
            size = 2;
            scaledHealth = 200;
            range = 420f;
            minRange = 60f;
            reload = 210f;
            recoil = 1f;
            rotateSpeed = 2.5f;
            shootCone = 6f;
            targetAir = false;
            shootSound = Sounds.missile;
            shootEffect = DGFx.markerShoot;
            smokeEffect = Fx.none;
            drawer = new DrawEmptyTurret();
            coolant = consumeCoolant(0.2f);

            ammo(
                    Items.blastCompound, new MarkerBulletType(8f, 10, new StrikeBulletType(260f, 48f)){{
                        lifetime = 53f;
                        width = 5f;
                        height = 10f;
                        frontColor = Color.white;
                        backColor = hitColor = trailColor = DGFx.strikeColor;
                        trailLength = 8;
                        trailWidth = 1.2f;
                        hitEffect = DGFx.markerShoot;
                    }},
                    Items.pyratite, new MarkerBulletType(8f, 10, new StrikeBulletType(190f, 44f){{
                        status = StatusEffects.burning;
                        statusDuration = 60f * 8f;
                        makeFire = true;
                        incendAmount = 12;
                        incendSpread = 26f;
                        incendChance = 1f;
                        hitEffect = DGFx.strikeBoomFire;
                        color = Pal.lightOrange;
                    }}){{
                        lifetime = 53f;
                        width = 5f;
                        height = 10f;
                        ammoMultiplier = 2f;
                        frontColor = Pal.lightishOrange;
                        backColor = hitColor = trailColor = Pal.lightOrange;
                        trailLength = 8;
                        trailWidth = 1.2f;
                        hitEffect = DGFx.markerShoot;
                    }}
            );
        }};

        capacitor = new CapacitorTurret("capacitor"){{
            requirements(Category.turret, with(
                    Items.copper, 120,
                    Items.lead, 110,
                    Items.silicon, 100,
                    Items.titanium, 60,
                    Items.surgeAlloy, 20
            ));
            size = 2;
            scaledHealth = 210;
            range = 190f;
            reload = 45f;
            recoil = 0f;
            rotateSpeed = 8f;
            shootCone = 30f;
            maxStacks = 12;
            stackTime = 40f;
            releaseInterval = 4f;
            shootSound = Sounds.lasershoot;
            shootEffect = DGFx.capacitorShoot;
            smokeEffect = Fx.none;
            consumePower(5f);
            coolant = consumeCoolant(0.2f);

            shootType = new BasicBulletType(4f, 20){{
                lifetime = 50f;
                width = 7f;
                height = 7f;
                sprite = "circle-bullet";
                homingPower = 0.12f;
                homingRange = 80f;
                splashDamage = 12f;
                splashDamageRadius = 14f;
                frontColor = Color.white;
                backColor = hitColor = trailColor = DGFx.gold;
                trailLength = 6;
                trailWidth = 1.8f;
                hitEffect = despawnEffect = DGFx.capacitorHit;
                buildingDamageMultiplier = 0.35f;
            }};
        }};

        smokeTest = new SmokeTestBlock("smoke-test"){{
            requirements(Category.effect, BuildVisibility.sandboxOnly, with());
            size = 2;
            health = 400;
            smokeX = -3f;
            smokeY = 3.5f;
        }};
    }
}