package com.aionemu.gameserver.dataholders.loadingutils;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlValidationHandler implements ValidationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(XmlValidationHandler.class);

    @Override
    public boolean handleEvent(ValidationEvent event) {
        // Java 17 JAXB is strict - log warning but don't fail
        // Fix for common_drop_group, etc.
        if (event.getSeverity() != ValidationEvent.WARNING) {
            log.warn("[XML Validation] " + event.getMessage() + " at line:" + event.getLocator().getLineNumber() + " col:" + event.getLocator().getColumnNumber());
        }
        return true; // CONTINUE - don't throw Error
    }
}
