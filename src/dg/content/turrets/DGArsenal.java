package dg.content.turrets;

import arc.graphics.Color;
import dg.content.*;
import dg.entities.bullet.*;
import dg.world.blocks.defense.*;
import dg.world.blocks.turrets.*;
import dg.world.radar.*;
import dg.world.deep.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.*;

import static mindustry.type.ItemStack.with;

public class DGArsenal{
    public static Block
        gravitor, permafrost, glaciate, repulsor, lineal, barrage, miasma,
        aegis, drizzle, ricochet, rotary, nest, sower, lantern, radar, skyguard, seismicScanner, abyssBore;

    public static void load(){
        gravitor = new WaveTower("gravitor"){{
            requirements(Category.turret, with(Items.lead, 120, Items.silicon, 90, Items.titanium, 60, Items.graphite, 40));
            size = 2;
            health = 800;
            range = 150f;
            reload = 130f;
            waveSpeed = 2.4f;
            force = 7.5f;
            chevrons = 14;
            color = DGTurretFx.pull;
            waveSound = Sounds.wave;
            hitEffect = DGTurretFx.bulletTurn;
            consumePower(3.5f);
        }};

        permafrost = new AuraTower("permafrost"){{
            requirements(Category.turret, with(Items.copper, 90, Items.lead, 70, Items.metaglass, 40, Items.titanium, 30));
            size = 2;
            health = 700;
            range = 95f;
            status = DGStatus.chilled;
            statusDuration = 50f;
            color = DGTurretFx.frost;
            moteEffect = DGTurretFx.frostMote;
            moteChance = 0.35f;
            consumePower(1.6f);
        }};

        glaciate = new WaveTower("glaciate"){{
            requirements(Category.turret, with(Items.lead, 140, Items.titanium, 90, Items.metaglass, 70, Items.silicon, 60));
            size = 2;
            health = 900;
            range = 125f;
            reload = 260f;
            waveSpeed = 2f;
            damage = 12f;
            status = DGStatus.frozen;
            statusDuration = 150f;
            color = DGTurretFx.frost;
            waveSound = Sounds.release;
            hitEffect = DGTurretFx.iceShatter;
            consumePower(4f);
        }};

        repulsor = new WaveTower("repulsor"){{
            requirements(Category.turret, with(Items.lead, 120, Items.silicon, 90, Items.titanium, 60, Items.graphite, 40));
            size = 2;
            health = 800;
            range = 110f;
            reload = 110f;
            waveSpeed = 3f;
            force = -6f;
            chevrons = 14;
            toggleGround = true;
            color = DGTurretFx.push;
            waveSound = Sounds.wave;
            hitEffect = DGTurretFx.bulletTurn;
            consumePower(3.5f);
        }};

        lineal = new FixedLaserTurret("lineal"){{
            requirements(Category.turret, with(Items.copper, 120, Items.lead, 80, Items.silicon, 70, Items.titanium, 40));
            size = 2;
            health = 900;
            range = 260f;
            reload = 70f;
            shootCone = 4f;
            recoil = 2f;
            targetAir = true;
            shoot.firstShotDelay = 35f;
            shootSound = Sounds.laser;
            chargeSound = Sounds.lasercharge;
            consumePower(6f);
            coolant = consumeCoolant(0.2f);

            shootType = new LaserBulletType(110f){{
                length = 270f;
                width = 16f;
                colors = new Color[]{Color.valueOf("ff6a6a66"), Color.valueOf("ff6a6a"), Color.white};
                hitColor = Color.valueOf("ff6a6a");
                chargeEffect = DGTurretFx.laserCharge;
                buildingDamageMultiplier = 0.4f;
            }};
        }};

        barrage = new ItemTurret("barrage"){{
            requirements(Category.turret, with(Items.copper, 200, Items.graphite, 150, Items.titanium, 100, Items.silicon, 80));
            size = 3;
            health = 1600;
            range = 270f;
            reload = 85f;
            rotateSpeed = 2.5f;
            recoil = 2f;
            shoot = new ShootAlternate(8f){{
                shots = 2;
                shotDelay = 12f;
            }};
            shootSound = Sounds.missileLaunch;
            shootEffect = Fx.shootBig;
            smokeEffect = DGFx.arcflashSmoke;

            ammo(
                Items.blastCompound, missile(Pal.missileYellow, Pal.missileYellowBack, false),
                Items.pyratite, missile(Pal.lightishOrange, Pal.lightOrange, true)
            );
        }};

        miasma = new ItemTurret("miasma"){{
            requirements(Category.turret, with(Items.copper, 90, Items.lead, 80, Items.metaglass, 50, Items.graphite, 40));
            size = 2;
            health = 750;
            range = 130f;
            reload = 22f;
            inaccuracy = 8f;
            velocityRnd = 0.2f;
            rotateSpeed = 4f;
            targetAir = true;
            shootSound = Sounds.steam;
            shootEffect = Fx.none;
            smokeEffect = Fx.none;

            ammo(
                Items.sporePod, new GasCloudBulletType(3.6f){{
                    color = DGTurretFx.toxic;
                    dark = Color.valueOf("4f6b38");
                    gas = DGStatus.toxic;
                    gasDuration = 180f;
                    dps = 8f;
                    ammoMultiplier = 3f;
                }},
                Items.pyratite, new GasCloudBulletType(3.6f){{
                    color = Color.valueOf("ffb066");
                    dark = Color.valueOf("8a4a2a");
                    gas = StatusEffects.burning;
                    gasDuration = 120f;
                    dps = 10f;
                    ignites = true;
                    ammoMultiplier = 3f;
                }},
                Items.coal, new GasCloudBulletType(3.6f){{
                    color = Color.valueOf("6b6b75");
                    dark = Color.valueOf("2e2e35");
                    gas = StatusEffects.slow;
                    gasDuration = 150f;
                    dps = 3f;
                    ammoMultiplier = 2f;
                }}
            );
        }};

        aegis = new WaveTower("aegis"){{
            requirements(Category.turret, with(Items.lead, 150, Items.silicon, 120, Items.titanium, 90, Items.plastanium, 40));
            size = 3;
            health = 1400;
            range = 120f;
            reload = 45f;
            waveSpeed = 4f;
            bullets = true;
            color = DGTurretFx.shield;
            waveSound = Sounds.shield;
            hitEffect = DGTurretFx.bulletPop;
            hasItems = true;
            itemCapacity = 10;
            consumePower(5f);
            consumeItem(Items.phaseFabric).boost();
        }};

        drizzle = new LiquidTurret("drizzle"){{
            requirements(Category.turret, with(Items.copper, 60, Items.lead, 50, Items.metaglass, 30));
            size = 2;
            health = 600;
            range = 150f;
            reload = 10f;
            inaccuracy = 3f;
            rotateSpeed = 6f;
            targetAir = false;
            shoot = new ShootAlternate(4f);
            shootSound = Sounds.splash;
            shootEffect = Fx.none;

            ammo(
                Liquids.water, new DropletBulletType(Liquids.water){{
                    damage = 5f;
                    knockback = 1.6f;
                    speed = 4.6f;
                    lifetime = 34f;
                }},
                Liquids.slag, new DropletBulletType(Liquids.slag){{
                    damage = 14f;
                    speed = 4.6f;
                    lifetime = 34f;
                }},
                Liquids.cryofluid, new DropletBulletType(Liquids.cryofluid){{
                    damage = 7f;
                    speed = 4.6f;
                    lifetime = 34f;
                }},
                Liquids.oil, new DropletBulletType(Liquids.oil){{
                    damage = 5f;
                    speed = 4.6f;
                    lifetime = 34f;
                }}
            );
        }};

        ricochet = new PowerTurret("ricochet"){{
            requirements(Category.turret, with(Items.copper, 100, Items.lead, 90, Items.silicon, 70, Items.titanium, 40));
            size = 2;
            health = 800;
            range = 180f;
            reload = 55f;
            rotateSpeed = 5f;
            targetAir = true;
            targetGround = true;
            predictTarget = false;
            buildingFilter = b -> false;
            shootSound = Sounds.spark;
            shootEffect = DGTurretFx.bulletTurn;
            consumePower(4f);

            shootType = new BounceOrbBulletType(2.4f, 34f){{
                bounces = 5;
                bounceSlow = 0.78f;
                bounceRange = 120f;
                lifetime = 80f;
            }};
        }};

        rotary = new MinigunTurret("rotary"){{
            requirements(Category.turret, with(Items.copper, 150, Items.graphite, 90, Items.titanium, 70, Items.silicon, 50));
            size = 2;
            health = 1000;
            range = 175f;
            reload = 6f;
            rotateSpeed = 6f;
            recoil = 1f;
            shootCone = 6f;
            shoot = new ShootAlternate(3.5f);
            shootSound = Sounds.bolt;
            ammoUseEffect = DGTurretFx.shellCasing;
            heatColor = Color.valueOf("f9350f");
            coolant = consumeCoolant(0.2f);

            ammo(
                Items.graphite, rail(Color.valueOf("feb380"), 16f, 0.7f),
                Items.silicon, rail(Color.valueOf("c3d6ff"), 13f, 0.8f),
                Items.thorium, rail(Color.valueOf("f5a3c7"), 26f, 0.85f)
            );
        }};

        nest = new ItemTurret("nest"){{
            requirements(Category.turret, with(Items.copper, 110, Items.graphite, 70, Items.silicon, 70, Items.titanium, 30));
            size = 2;
            health = 850;
            range = 230f;
            reload = 75f;
            rotateSpeed = 4f;
            shootCone = 40f;
            predictTarget = false;
            velocityRnd = 0.35f;
            shoot = new ShootSpread(5, 8f);
            shootSound = Sounds.missileSmall;

            ammo(
                Items.silicon, new SeekerBulletType(3.2f, 20f){{
                    color = Color.valueOf("ffb3e6");
                    ammoMultiplier = 3f;
                }},
                Items.graphite, new SeekerBulletType(3.2f, 28f){{
                    color = Color.valueOf("b9c0ff");
                    seekSpeed = 6f;
                    ammoMultiplier = 2f;
                }}
            );
        }};

        sower = new ItemTurret("sower"){{
            requirements(Category.turret, with(Items.copper, 120, Items.graphite, 80, Items.titanium, 50));
            size = 2;
            health = 800;
            range = 200f;
            reload = 160f;
            rotateSpeed = 3f;
            targetAir = false;
            shootSound = Sounds.artillery;
            shootEffect = Fx.shootSmall;

            ammo(
                Items.blastCompound, mineShell(Pal.remove, 70f, 26f, 7),
                Items.graphite, mineShell(Color.valueOf("ffd27a"), 38f, 20f, 5)
            );
        }};

        lantern = new PowerTurret("lantern"){{
            requirements(Category.turret, with(Items.copper, 80, Items.lead, 60, Items.silicon, 40, Items.metaglass, 30));
            size = 2;
            health = 700;
            range = 150f;
            reload = 16f;
            inaccuracy = 12f;
            velocityRnd = 0.35f;
            rotateSpeed = 5f;
            targetAir = true;
            shootSound = Sounds.pew;
            consumePower(2.5f);

            shootType = new HoverOrbBulletType(5.4f, 14f){{
                color = DGTurretFx.orb;
            }};
        }};
        radar = new RadarBlock("radar"){{
            requirements(Category.effect, with(Items.copper, 80, Items.lead, 60, Items.silicon, 50));
            size = 2;
            health = 500;
            range = 260f;
            consumePower(1.2f);
        }};

        seismicScanner = new SeismicScanner("seismic-scanner"){{
            requirements(Category.production, with(Items.copper, 60, Items.lead, 50, Items.graphite, 30));
            size = 2;
            health = 400;
            consumePower(0.8f);
        }};

        abyssBore = new AbyssBore("abyss-bore"){{
            requirements(Category.production, with(Items.copper, 150, Items.graphite, 80, Items.silicon, 60, Items.titanium, 50));
            size = 3;
            health = 900;
            consumePower(2.5f);
        }};

        skyguard = new SamTurret("skyguard"){{
            requirements(Category.turret, with(Items.copper, 120, Items.graphite, 80, Items.silicon, 80, Items.titanium, 50));
            size = 2;
            health = 900;
            range = 110f;
            networkRange = 640f;
            reload = 55f;
            rotateSpeed = 8f;
            shootCone = 35f;
            predictTarget = false;
            shoot = new ShootAlternate(5f){{
                shots = 2;
                shotDelay = 8f;
            }};
            shootSound = Sounds.missileLaunch;
            shootEffect = Fx.shootSmallSmoke;

            ammo(
                Items.silicon, sam(Pal.missileYellow, Pal.missileYellowBack, 70f, 30f, 28f),
                Items.blastCompound, sam(Pal.lightishOrange, Pal.lightOrange, 45f, 75f, 44f)
            );
        }};
    }

    static BulletType sam(Color fc, Color bc, float dmg, float splash, float rad){
        return new SamMissileBulletType(4.6f, dmg){{
            width = 8f;
            height = 13f;
            frontColor = fc;
            backColor = bc;
            trailColor = bc;
            trailLength = 12;
            trailWidth = 1.6f;
            splashDamage = splash;
            splashDamageRadius = rad;
            hitEffect = DGTurretFx.clusterBoom;
            despawnEffect = Fx.none;
            hitSound = Sounds.explosion;
            ammoMultiplier = 2f;
        }};
    }

    static BulletType missile(Color fc, Color bc, boolean incendiary){
        return new MissileBulletType(3.4f, 18f){{
            width = 10f;
            height = 15f;
            lifetime = 80f;
            frontColor = fc;
            backColor = bc;
            trailColor = bc;
            splashDamage = 35f;
            splashDamageRadius = 26f;
            despawnHit = true;
            homingPower = 0.06f;
            hitEffect = DGTurretFx.clusterBoom;
            despawnEffect = mindustry.content.Fx.none;
            hitSound = Sounds.explosion;
            ammoMultiplier = 1f;
            fragBullets = 9;
            fragVelocityMin = 0.5f;
            fragLifeMin = 0.6f;
            fragBullet = new BasicBulletType(4.4f, incendiary ? 18f : 26f){{
                width = 7f;
                height = 9f;
                lifetime = 20f;
                frontColor = fc;
                backColor = bc;
                trailColor = bc;
                trailLength = 5;
                trailWidth = 1.5f;
                splashDamage = incendiary ? 14f : 34f;
                splashDamageRadius = incendiary ? 16f : 22f;
                despawnHit = true;
                if(incendiary){
                    status = StatusEffects.burning;
                    statusDuration = 240f;
                    makeFire = true;
                    incendAmount = 2;
                    incendSpread = 10f;
                    incendChance = 0.7f;
                    hitEffect = Fx.fireHit;
                }else{
                    status = StatusEffects.blasted;
                    hitEffect = Fx.blastExplosion;
                    buildingDamageMultiplier = 1.4f;
                }
            }};
        }};
    }

    static BulletType rail(Color col, float dmg, float pf){
        return new RailBulletType(){{
            length = 180f;
            damage = dmg;
            hitColor = col;
            pierceDamageFactor = pf;
            hitEffect = DGTurretFx.railEnd;
            endEffect = DGTurretFx.railEnd;
            shootEffect = DGTurretFx.railShoot;
            smokeEffect = Fx.colorSpark;
            lineEffect = DGTurretFx.railTrail;
            ammoMultiplier = 3f;
        }};
    }

    static BulletType mineShell(Color col, float dmg, float rad, int n){
        return new BasicBulletType(1.15f, 0f){{
            lifetime = 175f;
            width = 11f;
            height = 11f;
            shrinkY = 0f;
            spin = 4f;
            sprite = "shell";
            frontColor = Color.white;
            backColor = col;
            collides = false;
            collidesTiles = false;
            collidesAir = false;
            hittable = false;
            despawnHit = true;
            hitEffect = Fx.none;
            despawnEffect = Fx.none;
            hitSound = Sounds.mineDeploy;
            fragBullets = n;
            fragVelocityMin = 0.3f;
            fragBullet = new MineBulletType(dmg, rad){{
                light = col;
            }};
        }};
    }
}
