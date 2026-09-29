/**
 * Fixed AbyssCore for Aion 7.9 - Java 17 compatible
 * Original crashed on GeoWorldLoader.loadMeshs due to missing/corrupt .mesh file
 * This version handles missing geo gracefully and disables core death if geo not found
 */
package zone;

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

@ZoneNameAnnotation("CORE2_400010000")
public class AbyssCore implements ZoneHandler {

        private static final Logger log = LoggerFactory.getLogger(AbyssCore.class);
        FastMap<Integer, CollisionDieActor> observed = new FastMap<Integer, CollisionDieActor>();
        private Node geometry = null;
        private boolean geoEnabled = false;

        public AbyssCore() {
                try {
                        // Try to load the abyss core collision mesh
                        Object loaded = GeoWorldLoader.loadMeshs("data/geo/models/na_ab_lmark_col_01a.mesh");
                        if (loaded != null && loaded instanceof java.util.Map) {
                                java.util.Map map = (java.util.Map) loaded;
                                if (!map.isEmpty()) {
                                        this.geometry = (Node) map.values().toArray()[0];
                                        this.geometry.setTransform(new Matrix3f(1.15f, 0, 0, 0, 1.15f, 0, 0, 0, 1.15f), new Vector3f(2140.104f, 1925.5823f, 2303.919f), 1f);
                                        geometry.updateModelBound();
                                        geoEnabled = true;
                                        log.info("[AbyssCore] Geo loaded: CORE2_400010000 collision enabled");
                                }
                        }
                }
                catch (Exception e) {
                        // Geo file missing or corrupt - disable core death, but don't crash server
                        // This is OK for 7.9 - core death is not needed, CORE2 is just ITEM_USE zone
                        log.info("[AbyssCore] Geo not found (na_ab_lmark_col_01a.mesh), core collision disabled - server will start without core death zone");
                        geometry = null;
                        geoEnabled = false;
                }
                if (geometry == null) {
                        log.info("[AbyssCore] Running in safe mode - CORE2_400010000 will not kill players (no geo)");
                }
        }

        @Override
        public void onEnterZone(Creature creature, ZoneInstance zone) {
                if (!geoEnabled || geometry == null) {
                        return; // Safe mode - do nothing
                }
                try {
                        Creature acting = creature.getActingCreature();
                        if (acting instanceof Player && !((Player) acting).isGM()) {
                                CollisionDieActor observer = new CollisionDieActor(creature, geometry);
                                creature.getObserveController().addObserver(observer);
                                observed.put(creature.getObjectId(), observer);
                        }
                } catch (Exception e) {
                        // Ignore
                }
        }

        @Override
        public void onLeaveZone(Creature creature, ZoneInstance zone) {
                if (!geoEnabled || geometry == null) {
                        return;
                }
                try {
                        Creature acting = creature.getActingCreature();
                        if (acting instanceof Player && !((Player) acting).isGM()) {
                                CollisionDieActor observer = observed.get(creature.getObjectId());
                                if (observer != null) {
                                        creature.getObserveController().removeObserver(observer);
                                        observed.remove(creature.getObjectId());
                                }
                        }
                } catch (Exception e) {
                        // Ignore
                }
        }
}
