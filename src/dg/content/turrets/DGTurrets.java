package dg.content.turrets;

import arc.graphics.Color;
import arc.math.Interp;
import dg.content.DGFx;
import dg.entities.bullet.*;
import dg.entities.part.*;
import dg.world.blocks.SmokeTestBlock;
import dg.world.draw.DrawPrismTurret;
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

            stormcoil, quake, glacier, hornet,

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

        stormcoil = new PowerTurret("stormcoil"){{
            requirements(Category.turret, with(
                    Items.copper, 150,
                    Items.lead, 120,
                    Items.graphite, 50,
                    Items.silicon, 90,
                    Items.titanium, 60
            ));
            size = 2;
            scaledHealth = 200;
            range = 150f;
            reload = 50f;
            recoil = 0f;
            shootY = 0f;
            shootCone = 50f;
            rotateSpeed = 5f;
            targetAir = false;
            heatColor = Pal.lancerLaser;
            shootSound = Sounds.spark;
            shootEffect = Fx.none;
            smokeEffect = Fx.none;
            consumePower(7f);
            coolant = consumeCoolant(0.2f);

            shootType = new ChainLightningBulletType(){{
                damage = 34;
                lightningLength = 24;
                chainDamageFalloff = 0.12f;
                maxChains = 5;
                chainEffect = DGFx.chainArc;
                chainZ = 10.5f;
                hitEffect = DGFx.arcHit;
                collidesAir = false;
                buildingDamageMultiplier = 0.3f;
            }};

            Color copperLight = Color.valueOf("f0b27a"), copperDark = Color.valueOf("8a5530"), copper = Color.valueOf("d99f6b");
            PartProgress ready = PartProgress.reload.inv();
            PartProgress armed = PartProgress.warmup.mul(ready);

            drawer = new DrawPrismTurret(){{
                parts.addAll(
                        new PrismPart(){{
                            verts = regular(8, 7.2f, 22.5f);
                            z1 = 1.6f;
                            top = Color.valueOf("6a6b75");
                        }},
                        new PrismPart(){{
                            verts = rect(0f, 0f, 1.8f, 4.5f);
                            mirror = true;
                            x = 4.2f;
                            y = 2.8f;
                            z0 = 1.6f;
                            z1 = 3.6f;
                            moveX = 0.6f;
                            top = copper;
                            light = copperLight;
                            dark = copperDark;
                            glow = Pal.lancerLaser;
                            glowProgress = PartProgress.heat.add(armed.mul(0.25f));
                        }},
                        new TubePart(){{
                            z1 = 1.6f;
                            z2 = 2.8f;
                            radius = 3.6f;
                            capColor = Color.valueOf("5a5b66");
                        }},
                        new TubePart(){{
                            z1 = 2.8f;
                            z2 = 4f;
                            radius = 1f;
                        }},
                        new TubePart(){{
                            z1 = 4f;
                            z2 = 4.7f;
                            radius = 3f;
                            lightColor = copperLight;
                            darkColor = copperDark;
                            capColor = copper;
                        }},
                        new TubePart(){{
                            z1 = 4.7f;
                            z2 = 5.9f;
                            radius = 0.9f;
                        }},
                        new TubePart(){{
                            z1 = 5.9f;
                            z2 = 6.6f;
                            radius = 2.6f;
                            lightColor = copperLight;
                            darkColor = copperDark;
                            capColor = copper;
                        }},
                        new TubePart(){{
                            z1 = 6.6f;
                            z2 = 7.8f;
                            radius = 0.8f;
                        }},
                        new TubePart(){{
                            z1 = 7.8f;
                            z2 = 8.5f;
                            radius = 2.2f;
                            lightColor = copperLight;
                            darkColor = copperDark;
                            capColor = copper;
                        }},
                        new TubePart(){{
                            z1 = 8.5f;
                            z2 = 9.6f;
                            radius = 0.7f;
                        }},
                        new ArcPart(){{
                            mirror = true;
                            x1 = 4.2f;
                            y1 = 4.8f;
                            z1 = 3.6f;
                            x2 = 2.6f;
                            y2 = 0.6f;
                            z2 = 4.6f;
                            progress = armed;
                            chance = 0.45f;
                            jitter = 0.7f;
                            stroke = 0.6f;
                        }},
                        new ArcPart(){{
                            mirror = true;
                            x1 = 2f;
                            z1 = 8.4f;
                            z2 = 10.5f;
                            progress = armed;
                            chance = 0.5f;
                            jitter = 0.6f;
                            stroke = 0.55f;
                        }},
                        new OrbPart(){{
                            z = 10.5f;
                            radius = 1f;
                            radiusTo = 1.9f;
                            progress = ready;
                            alphaProgress = PartProgress.warmup.mul(0.6f).add(0.4f);
                            coreColor = Color.valueOf("e8efff");
                            pulseScl = 4f;
                            pulseMag = 0.1f;
                            pool = 3f;
                        }},
                        new OrbPart(){{
                            z = 10.5f;
                            radius = 0f;
                            radiusTo = 3.2f;
                            progress = PartProgress.heat.curve(Interp.pow2Out);
                            alphaProgress = PartProgress.heat;
                            spikes = 1.8f;
                            spikeRotateSpeed = 3f;
                            pool = 0f;
                        }},
                        new OrbitPart(){{
                            z = 10.5f;
                            count = 4;
                            radius = 3.4f;
                            tilt = 60f;
                            spinSpeed = 4f;
                            shardWidth = 0.8f;
                            shardLength = 1.5f;
                            color = Color.white;
                            backColor = Pal.lancerLaser.cpy().mul(0.7f);
                            progress = PartProgress.warmup;
                            alphaProgress = PartProgress.warmup;
                        }}
                );
            }};
        }};

        quake = new ItemTurret("quake"){{
            requirements(Category.turret, with(
                    Items.copper, 180,
                    Items.graphite, 140,
                    Items.silicon, 60,
                    Items.titanium, 90
            ));
            size = 2;
            scaledHealth = 240;
            range = 300f;
            minRange = 40f;
            reload = 95f;
            recoil = 2.5f;
            rotateSpeed = 1.6f;
            inaccuracy = 3f;
            shootCone = 10f;
            shootY = 7f;
            shake = 2.5f;
            targetAir = false;
            shootSound = Sounds.artillery;
            ammoUseEffect = DGFx.quakeCasing;
            shootEffect = DGFx.quakeShoot;
            smokeEffect = DGFx.quakeSmoke;
            coolant = consumeCoolant(0.2f);

            Color amber = Color.valueOf("e8a04c"), amberLight = Color.valueOf("ffe0b0");

            ammo(
                    Items.graphite, new LobBulletType(2.6f, 20){{
                        lifetime = 115f;
                        width = height = 13f;
                        splashDamage = 70f;
                        splashDamageRadius = 32f;
                        knockback = 1f;
                        hitEffect = DGFx.quakeBurst;
                        despawnEffect = Fx.none;
                        hitColor = backColor = trailColor = amber;
                        frontColor = amberLight;
                        trailLength = 10;
                        trailWidth = 1.6f;
                    }},
                    Items.silicon, new LobBulletType(2.6f, 20){{
                        lifetime = 115f;
                        width = height = 13f;
                        splashDamage = 70f;
                        splashDamageRadius = 32f;
                        knockback = 1f;
                        homingPower = 0.06f;
                        homingRange = 70f;
                        reloadMultiplier = 1.2f;
                        ammoMultiplier = 3f;
                        hitEffect = DGFx.quakeBurst;
                        despawnEffect = Fx.none;
                        hitColor = backColor = trailColor = amber;
                        frontColor = amberLight;
                        trailLength = 10;
                        trailWidth = 1.6f;
                    }},
                    Items.blastCompound, new LobBulletType(2.4f, 25){{
                        lifetime = 125f;
                        width = height = 15f;
                        splashDamage = 110f;
                        splashDamageRadius = 44f;
                        knockback = 1.5f;
                        status = StatusEffects.blasted;
                        reloadMultiplier = 0.8f;
                        ammoMultiplier = 2f;
                        arcHeight = 55f;
                        hitEffect = DGFx.quakeBurstBlast;
                        despawnEffect = Fx.none;
                        hitColor = backColor = trailColor = Pal.missileYellowBack;
                        frontColor = Pal.missileYellow;
                        trailLength = 12;
                        trailWidth = 1.9f;
                    }}
            );

            drawer = new DrawTurret(){{
                parts.addAll(
                        new LiftPart("-barrel"){{
                            progress = PartProgress.recoil.curve(Interp.pow2In);
                            moveY = -2.5f;
                            lift = 2f;
                            liftProgress = PartProgress.warmup.curve(Interp.smooth);
                            shadowAlpha = 0.75f;
                            heatColor = Color.valueOf("f06a1a");
                            heatLight = true;
                        }},
                        new RisePart(){{
                            y = 7f;
                            spread = 1f;
                            particles = 4;
                            lifetime = 50f;
                            rise = 9f;
                            drift = 2.5f;
                            size = 0.9f;
                            sizeTo = 2.2f;
                            alpha = 0.5f;
                            color = Color.valueOf("a5a6ad");
                            colorTo = DGFx.smokeColor;
                            progress = PartProgress.heat;
                            layer = Layer.bullet - 1f;
                        }}
                );
            }};
        }};

        glacier = new LiquidTurret("glacier"){{
            requirements(Category.turret, with(
                    Items.metaglass, 120,
                    Items.lead, 150,
                    Items.silicon, 90,
                    Items.titanium, 110,
                    Items.plastanium, 40
            ));
            size = 2;
            scaledHealth = 220;
            range = 205f;
            reload = 110f;
            recoil = 1.5f;
            rotateSpeed = 3f;
            shootY = 6f;
            liquidCapacity = 60f;
            targetAir = false;
            extinguish = false;
            shootSound = Sounds.malignShoot;
            loopSound = Sounds.none;
            shootEffect = DGFx.glacierShoot;
            smokeEffect = DGFx.frostPuff;
            consumePower(3f);

            Color cryo = Color.valueOf("afeeee");

            ammo(
                    Liquids.cryofluid, new FrostOrbBulletType(2.2f, 45){{
                        lifetime = 95f;
                        hitSize = 7f;
                        splashDamage = 40f;
                        splashDamageRadius = 30f;
                        status = StatusEffects.freezing;
                        statusDuration = 240f;
                        collidesAir = false;
                        ammoMultiplier = 0.3f;
                        hitEffect = despawnEffect = DGFx.glacierBurst;
                        trailLength = 8;
                        trailWidth = 2.4f;
                        trailColor = cryo;

                        fragBullets = 7;
                        fragRandomSpread = 360f;
                        fragVelocityMin = 0.7f;
                        fragBullet = new BasicBulletType(3.2f, 12){{
                            width = 5f;
                            height = 9f;
                            lifetime = 14f;
                            frontColor = Color.white;
                            backColor = cryo;
                            status = StatusEffects.freezing;
                            statusDuration = 60f;
                            collidesAir = false;
                            hitEffect = despawnEffect = DGFx.shrapnelHit;
                            hitColor = cryo;
                        }};
                    }}
            );

            PartProgress ready = PartProgress.reload.inv();

            drawer = new DrawPrismTurret(){{
                parts.addAll(
                        new PrismPart(){{
                            verts = regular(6, 7.4f, 0f);
                            z1 = 1.8f;
                            top = Color.valueOf("5c6070");
                            light = Color.valueOf("8a8fa0");
                            dark = Color.valueOf("34374a");
                        }},
                        new RisePart(){{
                            mirror = true;
                            x = 4.5f;
                            y = -3f;
                            spread = 1.5f;
                            particles = 4;
                            lifetime = 80f;
                            rise = 7f;
                            drift = 2f;
                            size = 0.9f;
                            sizeTo = 2.2f;
                            alpha = 0.35f;
                            color = Color.white;
                            colorTo = cryo;
                            progress = PartProgress.warmup;
                            layer = Layer.bullet - 1f;
                        }},
                        new PrismPart(){{
                            verts = rect(0f, 0f, 2.2f, 9f);
                            mirror = true;
                            x = 3.6f;
                            y = 2f;
                            z0 = 1.8f;
                            z1 = 3.8f;
                            moveX = 0.5f;
                            recoilY = 1.5f;
                            top = Color.valueOf("6974c4");
                            light = Color.valueOf("8aa3f4");
                            dark = Color.valueOf("3c4580");
                            glow = cryo;
                            glowProgress = PartProgress.warmup.mul(ready).mul(0.5f);
                        }},
                        new PrismPart(){{
                            verts = regular(6, 2.6f, 30f);
                            y = -2f;
                            z0 = 1.8f;
                            z1 = 5.5f;
                            top = cryo;
                            light = Color.valueOf("e8ffff");
                            dark = Color.valueOf("6974c4");
                            insetColor = Color.valueOf("e8ffff");
                            glow = cryo;
                            glowProgress = PartProgress.warmup.mul(ready).mul(0.7f).add(PartProgress.heat);
                        }},
                        new OrbitPart(){{
                            y = 6f;
                            z = 4f;
                            count = 5;
                            radius = 6f;
                            radiusTo = 3f;
                            tilt = 60f;
                            spinSpeed = 3f;
                            spinProgress = 180f;
                            shardWidth = 1.2f;
                            shardLength = 2.6f;
                            color = Color.valueOf("d8f6ff");
                            backColor = Color.valueOf("6974c4");
                            progress = ready.curve(Interp.pow2Out);
                            alphaProgress = PartProgress.warmup.mul(ready.curve(0.3f, 0.7f).clamp());
                        }},
                        new OrbPart(){{
                            y = 6f;
                            z = 4f;
                            radius = 0f;
                            radiusTo = 2f;
                            color = cryo;
                            coreColor = Color.valueOf("d8ffff");
                            coreScl = 0.45f;
                            progress = ready.curve(Interp.pow2In);
                            alphaProgress = PartProgress.warmup;
                            spikes = 1.6f;
                            spikeRotateSpeed = 2f;
                        }}
                );
            }};
        }};

        hornet = new ItemTurret("hornet"){{
            requirements(Category.turret, with(
                    Items.copper, 130,
                    Items.graphite, 90,
                    Items.silicon, 80,
                    Items.titanium, 50
            ));
            size = 2;
            scaledHealth = 200;
            range = 250f;
            reload = 70f;
            recoil = 1f;
            rotateSpeed = 4f;
            inaccuracy = 6f;
            shootCone = 30f;
            shootSound = Sounds.missile;
            shootEffect = DGFx.hornetLaunch;
            smokeEffect = DGFx.missileSmoke;
            coolant = consumeCoolant(0.2f);

            shoot = new ShootBarrel(){{
                barrels = new float[]{
                        -2.2f, 2f, 0f,
                        2.2f, 2f, 0f,
                        -2.2f, -0.6f, 0f,
                        2.2f, -0.6f, 0f
                };
                shots = 4;
                shotDelay = 5f;
            }};

            Color smoke = Color.valueOf("a5a6ad");

            ammo(
                    Items.blastCompound, new MissileBulletType(3.4f, 14){{
                        lifetime = 75f;
                        width = 7f;
                        height = 9f;
                        splashDamage = 30f;
                        splashDamageRadius = 22f;
                        status = StatusEffects.blasted;
                        ammoMultiplier = 4f;
                        buildingDamageMultiplier = 0.4f;
                        trailChance = 0f;
                        trailInterval = 3f;
                        trailEffect = DGFx.missileSmoke;
                        trailColor = smoke;
                        hitEffect = despawnEffect = DGFx.hornetPop;
                    }},
                    Items.pyratite, new MissileBulletType(3.4f, 12){{
                        lifetime = 75f;
                        width = 7f;
                        height = 9f;
                        splashDamage = 22f;
                        splashDamageRadius = 18f;
                        status = StatusEffects.burning;
                        makeFire = true;
                        ammoMultiplier = 4f;
                        frontColor = Pal.lightishOrange;
                        backColor = Pal.lightOrange;
                        trailChance = 0f;
                        trailInterval = 3f;
                        trailEffect = DGFx.missileSmoke;
                        trailColor = smoke;
                        hitEffect = despawnEffect = DGFx.hornetPop;
                    }},
                    Items.silicon, new MissileBulletType(3.6f, 12){{
                        lifetime = 70f;
                        width = 7f;
                        height = 9f;
                        splashDamage = 20f;
                        splashDamageRadius = 16f;
                        homingPower = 0.15f;
                        reloadMultiplier = 1.3f;
                        ammoMultiplier = 5f;
                        trailChance = 0f;
                        trailInterval = 3f;
                        trailEffect = DGFx.missileSmoke;
                        trailColor = smoke;
                        hitEffect = despawnEffect = DGFx.hornetPop;
                    }}
            );

            Color red = Color.valueOf("da6b68"), orange = Color.valueOf("feb380"), redDark = Color.valueOf("8a3c3a");
            PartProgress armed = PartProgress.warmup.mul(PartProgress.reload.inv().curve(0.8f, 0.2f).clamp());

            drawer = new DrawPrismTurret(){{
                parts.addAll(
                        new PrismPart(){{
                            verts = new float[]{-6f, -6.5f, 6f, -6.5f, 7f, -5f, 7f, 4f, 5.5f, 6f, -5.5f, 6f, -7f, 4f, -7f, -5f};
                            z1 = 1.4f;
                            top = Color.valueOf("6a6b75");
                        }},
                        new PrismPart(){{
                            verts = rect(0f, 0f, 1.6f, 7f);
                            mirror = true;
                            x = 5.3f;
                            y = 0.5f;
                            z0 = 1.4f;
                            z1 = 3.4f;
                            recoilY = 0.8f;
                            top = red;
                            light = orange;
                            dark = redDark;
                        }},
                        new PrismPart(){{
                            verts = rect(0f, 0f, 9f, 9.5f);
                            y = 0.5f;
                            z0 = 1.4f;
                            z1 = 4.2f;
                            recoilY = 1.2f;
                            glow = orange;
                            glowProgress = PartProgress.heat.mul(0.6f);
                        }},
                        new TubePart(){{
                            mirror = true;
                            x = 2.2f;
                            y = 2f;
                            z1 = 4.2f;
                            z2 = 4.9f;
                            radius = 1.5f;
                            capColor = Color.valueOf("2c2d38");
                        }},
                        new TubePart(){{
                            mirror = true;
                            x = 2.2f;
                            y = -0.6f;
                            z1 = 4.2f;
                            z2 = 4.9f;
                            radius = 1.5f;
                            capColor = Color.valueOf("2c2d38");
                        }},
                        new OrbPart(){{
                            mirror = true;
                            x = 2.2f;
                            y = 2f;
                            z = 4.9f;
                            radius = 0.7f;
                            color = red;
                            coreColor = orange;
                            progress = PartProgress.constant(1f);
                            alphaProgress = armed;
                            pool = 0f;
                            lightRadius = 2f;
                        }},
                        new OrbPart(){{
                            mirror = true;
                            x = 2.2f;
                            y = -0.6f;
                            z = 4.9f;
                            radius = 0.7f;
                            color = red;
                            coreColor = orange;
                            progress = PartProgress.constant(1f);
                            alphaProgress = armed;
                            pool = 0f;
                            lightRadius = 2f;
                        }}
                );
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