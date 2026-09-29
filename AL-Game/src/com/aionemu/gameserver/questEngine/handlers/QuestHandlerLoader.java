package com.aionemu.gameserver.questEngine.handlers;

import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.ClassListener;
import com.aionemu.commons.utils.ClassUtils;
import com.aionemu.gameserver.questEngine.QuestEngine;

/**
 * FIXED FOR JAVA 17 - was using deprecated newInstance()
 * @author MrPoke - fixed by SaYber
 */
public class QuestHandlerLoader implements ClassListener {

        private static final Logger logger = LoggerFactory.getLogger(QuestHandlerLoader.class);

        public QuestHandlerLoader() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public void postLoad(Class<?>[] classes) {
                for (Class<?> c : classes) {
                        if (logger.isDebugEnabled()) {
                                logger.debug("Load class " + c.getName());
                        }

                        if (!isValidClass(c)) {
                                continue;
                        }

                        if (ClassUtils.isSubclass(c, QuestHandler.class)) {
                                try {
                                        Class<? extends QuestHandler> tmp = (Class<? extends QuestHandler>) c;
                                        if (tmp != null) {
                                                // JAVA 17 FIX: use getDeclaredConstructor().newInstance() instead of deprecated newInstance()
                                                QuestHandler handler = tmp.getDeclaredConstructor().newInstance();
                                                QuestEngine.getInstance().addQuestHandler(handler);
                                        }
                                }
                                catch (Exception e) {
                                        logger.error("Failed to load quest handler class: " + c.getName(), e);
                                        // Don't throw RuntimeException - log and continue, so other quests still load
                                        // throw new RuntimeException("Failed to load quest handler class: " + c.getName(), e);
                                }
                        }
                }
        }

        @Override
        public void preUnload(Class<?>[] classes) {
                if (logger.isDebugEnabled()) {
                        for (Class<?> c : classes) {
                                logger.debug("Unload class " + c.getName());
                        }
                }
                QuestEngine.getInstance().clear();
        }

        public boolean isValidClass(Class<?> clazz) {
                final int modifiers = clazz.getModifiers();
                if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                        return false;
                }
                if (!Modifier.isPublic(modifiers)) {
                        return false;
                }
                return true;
        }
}
