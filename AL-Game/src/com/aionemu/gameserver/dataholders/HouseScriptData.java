package com.aionemu.gameserver.dataholders;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aionemu.gameserver.model.templates.housing.LBox;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "lboxes")
public class HouseScriptData {

        private static final Logger log = LoggerFactory.getLogger(HouseScriptData.class);
        private static Marshaller marshaller;
        private static JAXBContext jc;

        static {
                try {
                        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        // Don't fail if xsd missing on Java 17
                        Schema schema = null;
                        try {
                            File xsd = new File("./data/static_data/housing/scripts.xsd");
                            if (xsd.exists()) {
                                schema = sf.newSchema(xsd);
                            }
                        } catch (Exception e) {
                            log.warn("Could not load housing xsd: " + e.getMessage());
                        }
                        jc = JAXBContext.newInstance(HouseScriptData.class);
                        marshaller = jc.createMarshaller();
                        if (schema != null) {
                            marshaller.setSchema(schema);
                        }
                        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                }
                catch (Exception e) {
                        log.error("Could not instantiate HouseScriptData : \n" + e, e);
                }
        }

        @XmlElement(name = "lbox", required = true)
        protected List<LBox> scriptData;
        @XmlTransient
        private final Map<Integer, LBox> defaultTemplates = new HashMap<Integer, LBox>();

        void afterUnmarshal(Unmarshaller u, Object parent) {
                if (scriptData != null) {
                    for (LBox template : scriptData) {
                            defaultTemplates.put(template.getId(), template);
                    }
                    scriptData.clear();
                    scriptData = null;
                }
        }

        public static class XmlFormatter {

                private static final Logger log = LoggerFactory.getLogger(XmlFormatter.class);
                private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                private static DocumentBuilder db;

                static {
                        try {
                                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
                                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
                                dbf.setValidating(false);
                                dbf.setNamespaceAware(true);
                                db = dbf.newDocumentBuilder();
                        }
                        catch (ParserConfigurationException e) {
                                log.error("Could not instantiate XmlFormatter : \n" + e, e);
                        }
                }

                public static String format(String unformattedXml) {
                        try {
                                // Strip BOM and leading whitespace that causes "Content is not allowed in prolog" on Java 17
                                if (unformattedXml != null) {
                                    unformattedXml = unformattedXml.replaceFirst("^\\uFEFF", "").trim();
                                    // remove any chars before <?xml
                                    int idx = unformattedXml.indexOf("<?xml");
                                    if (idx > 0) unformattedXml = unformattedXml.substring(idx);
                                }
                                final Document document = parseXmlFile(unformattedXml);
                                TransformerFactory tf = TransformerFactory.newInstance();
                                Transformer transformer = tf.newTransformer();
                                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                                Writer out = new StringWriter();
                                transformer.transform(new DOMSource(document), new StreamResult(out));
                                return out.toString();
                        }
                        catch (Exception e) {
                            log.error("Error formatting XML", e);
                        }
                        return null;
                }

                private static Document parseXmlFile(String in) {
                        try {
                                InputSource is = new InputSource(new StringReader(in));
                                return db.parse(is);
                        }
                        catch (SAXException e) {
                                throw new RuntimeException(e);
                        }
                        catch (IOException e) {
                                throw new RuntimeException(e);
                        }
                }
        }

        public String createScript(int scriptId, int position, int iconId) {
                LBox template = defaultTemplates.get(scriptId);
                if (template == null) return null;
                LBox result = (LBox) template.clone();
                result.setId(position);
                result.setIcon(iconId);

                HouseScriptData fragment = new HouseScriptData();
                fragment.scriptData = new ArrayList<LBox>();
                fragment.scriptData.add(result);

                Writer writer = new StringWriter();
                try {
                        marshaller.marshal(fragment, writer);
                }
                catch (JAXBException e) {
                    log.error("Marshal error", e);
                }
                return XmlFormatter.format(writer.toString());
        }

        public int size() {
                return defaultTemplates.size();
        }
}