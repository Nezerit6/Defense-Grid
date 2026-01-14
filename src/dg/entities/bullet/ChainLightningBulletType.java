package dg.entities.bullet;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Teamc;
import mindustry.graphics.Pal;

public class ChainLightningBulletType extends BulletType {
    public Color lightningColor;
    public int lightningLength;
    public int lightningLengthRand;
    public int maxChains = 3;
    public float chainDamageFalloff = 0.2f;

    public ChainLightningBulletType() {
        this.lightningColor = Pal.lancerLaser;
        this.lightningLength = 25;
        this.lightningLengthRand = 0;
        this.damage = 1.0F;
        this.speed = 0.0F;
        this.lifetime = 1.0F;
        this.despawnEffect = Fx.none;
        this.hitEffect = Fx.hitLancer;
        this.keepVelocity = false;
        this.hittable = false;
        this.status = StatusEffects.shocked;
    }

    protected float calculateRange() {
        return ((float) this.lightningLength + (float) this.lightningLengthRand / 2.0F) * 6.0F;
    }

    public float estimateDPS() {
        return super.estimateDPS() * Math.max((float) this.lightningLength / 10.0F, 1.0F);
    }

    public void draw(Bullet b) {
    }

    public void init(Bullet b) {
        super.init(b);

        float distance = this.lightningLength + Mathf.random(this.lightningLengthRand);
        float maxRange = distance * 6.0F;
        float chainRange = this.lightningLength * 6.0F;

        Seq<Teamc> hitTargets = new Seq<>();

        float startX = b.x;
        float startY = b.y;

        Teamc target = Units.closestTarget(b.team, startX, startY, maxRange, unit -> !unit.type.flying);

        for (int chain = 0; chain < maxChains && target != null; chain++) {
            final float fromX = chain == 0 ? startX : hitTargets.get(chain - 1).x();
            final float fromY = chain == 0 ? startY : hitTargets.get(chain - 1).y();
            final float toX = target.x();
            final float toY = target.y();

            Fx.chainLightning.at(fromX, fromY, 0f, lightningColor, target);

            float currentDamage = this.damage * Mathf.pow(1f - chainDamageFalloff, chain);
            Damage.collideLine(b, b.team, fromX, fromY,
                    Mathf.angle(toX - fromX, toY - fromY),
                    Mathf.dst(fromX, fromY, toX, toY), true, false);

            hitTargets.add(target);

            final Teamc[] nextTarget = {null};
            final float[] closestDist = {chainRange};

            Units.nearby(null, toX, toY, chainRange, unit -> {
                if (unit.team == b.team) return;
                if (unit.type.flying) return;
                if (hitTargets.contains(unit)) return;
                float dst = unit.dst(toX, toY);
                if (dst < closestDist[0]) {
                    closestDist[0] = dst;
                    nextTarget[0] = unit;
                }
            });

            Units.nearbyBuildings(toX, toY, chainRange, build -> {
                if (build.team == b.team) return;
                if (hitTargets.contains(build)) return;
                float dst = build.dst(toX, toY);
                if (dst < closestDist[0]) {
                    closestDist[0] = dst;
                    nextTarget[0] = build;
                }
            });

            target = nextTarget[0];
        }

        if (hitTargets.isEmpty()) {
            float angle = b.rotation();
            float targetX = b.x + Mathf.cosDeg(angle) * maxRange;
            float targetY = b.y + Mathf.sinDeg(angle) * maxRange;

            Fx.chainLightning.at(b.x, b.y, 0f, lightningColor, new Vec2(targetX, targetY));
            Damage.collideLine(b, b.team, b.x, b.y, angle, maxRange, true, false);
        }
    }
}