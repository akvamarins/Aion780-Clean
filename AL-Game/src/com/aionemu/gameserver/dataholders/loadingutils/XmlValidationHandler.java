package com.aionemu.gameserver.dataholders.loadingutils;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlValidationHandler implements ValidationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(XmlValidationHandler.class);

    @Override
    public boolean handleEvent(ValidationEvent event) {
        String msg = event.getMessage();
        
        // По феншую: common_drop_group теперь поддерживается в XmlNpcDrops.java, 
        // поэтому этот WARN больше не должен появляться.
        // Если вдруг где-то еще есть левые теги - не падаем, а просто логируем и продолжаем.
        // Java 17 JAXB строгий - возвращаем true чтобы не кидал Exception
        
        if (msg != null && msg.contains("common_drop_group")) {
            // После фикса XmlNpcDrops этот ивент уже не придет, но на всякий - подавляем спам
            log.debug("[XML Validation suppressed] {} at line:{} col:{}", msg, event.getLocator().getLineNumber(), event.getLocator().getColumnNumber());
            return true;
        }

        if (event.getSeverity() == ValidationEvent.WARNING) {
            log.warn("[XML Validation] {} at line:{} col:{}", msg, event.getLocator().getLineNumber(), event.getLocator().getColumnNumber());
        } else if (event.getSeverity() == ValidationEvent.ERROR) {
            log.warn("[XML Validation] {} at line:{} col:{}", msg, event.getLocator().getLineNumber(), event.getLocator().getColumnNumber());
        } else {
            log.error("[XML Validation FATAL] {} at line:{} col:{}", msg, event.getLocator().getLineNumber(), event.getLocator().getColumnNumber());
        }
        return true; // CONTINUE - не прерывать загрузку, иначе GS не стартанет
    }
}
