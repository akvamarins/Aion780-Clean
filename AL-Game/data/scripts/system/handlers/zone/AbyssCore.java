package zone;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.observer.CollisionDieActor;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;

import javolution.util.FastMap;

/**
 * @author MrPoke
 * @fix Java17 - safe geo loading, no ArrayIndexOutOfBounds, no var (script compiler is Java8 source level)
 */
@ZoneNameAnnotation("CORE_400010000")
public class AbyssCore implements ZoneHandler {

        private static final Logger log = LoggerFactory.getLogger(AbyssCore.class);
        FastMap<Integer, CollisionDieActor> observed = new FastMap<Integer, CollisionDieActor>();
        private Node geometry;

        public AbyssCore() {
                try {
                        Map<?, ?> meshs = GeoWorldLoader.loadMeshs("data/geo/models/na_ab_lmark_col_01a.mesh");
                        if (meshs == null || meshs.isEmpty()) {
                                log.warn("[JAVA17 FIX] AbyssCore geo empty - file data/geo/models/na_ab_lmark_col_01a.mesh returned no meshes (Invalid vectorCount). Skipping collision, zone will work without geo. OK when Geo is NO_GEO or file corrupted.");
                                this.geometry = null;
                                return;
                        }
                        Object first = meshs.values().toArray()[0];
                        if (!(first instanceof Node)) {
                                log.warn("[JAVA17 FIX] AbyssCore geo first element not a Node: " + first.getClass() + " - skipping");
                                this.geometry = null;
                                return;
                        }
                        this.geometry = (Node) first;
                        this.geometry.setTransform(new Matrix3f(1.15f, 0, 0, 0, 1.15f, 0, 0, 0, 1.15f), new Vector3f(2140.104f, 1925.5823f, 2303.919f), 1f);
                        geometry.updateModelBound();
                        log.info("[AbyssCore] Geometry loaded: na_ab_lmark_col_01a.mesh");
                }
                catch (IOException e) {
                        log.warn("[JAVA17 FIX] AbyssCore IOException loading mesh: " + e.getMessage() + " - zone will work without collision");
                        this.geometry = null;
                }
                catch (ArrayIndexOutOfBoundsException e) {
                        log.warn("[JAVA17 FIX] AbyssCore ArrayIndexOutOfBounds - mesh array empty (corrupted file). Zone works without geo. File: na_ab_lmark_col_01a.mesh");
                        this.geometry = null;
                }
                catch (Exception e) {
                        log.warn("[JAVA17 FIX] AbyssCore unexpected error loading geo: " + e.getMessage() + " - continuing without geo", e);
                        this.geometry = null;
                }
        }

        @Override
        public void onEnterZone(Creature creature, ZoneInstance zone) {
                if (geometry == null) {
                        return;
                }
                Creature acting = creature.getActingCreature();
                if (acting instanceof Player && !((Player) acting).isGM()) {
                        try {
                                CollisionDieActor observer = new CollisionDieActor(creature, geometry);
                                creature.getObserveController().addObserver(observer);
                                observed.put(creature.getObjectId(), observer);
                        } catch (Exception e) {
                                log.warn("[AbyssCore] onEnterZone error: " + e.getMessage());
                        }
                }
        }

        @Override
        public void onLeaveZone(Creature creature, ZoneInstance zone) {
                if (geometry == null) {
                        return;
                }
                Creature acting = creature.getActingCreature();
                if (acting instanceof Player && !((Player) acting).isGM()) {
                        try {
                                CollisionDieActor observer = observed.get(creature.getObjectId());
                                if (observer != null) {
                                        creature.getObserveController().removeObserver(observer);
                                        observed.remove(creature.getObjectId());
                                }
                        } catch (Exception e) {
                                log.warn("[AbyssCore] onLeaveZone error: " + e.getMessage());
                        }
                }
        }
}
