package com.helper.ingestion.util.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Parses XML into DOM documents with safe parser settings.
 */
public class XmlDocumentParser {
    private static final String DISALLOW_DOCTYPE_DECLARATION =
            "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String EXTERNAL_GENERAL_ENTITIES =
            "http://xml.org/sax/features/external-general-entities";
    private static final String EXTERNAL_PARAMETER_ENTITIES =
            "http://xml.org/sax/features/external-parameter-entities";

    public Document parse(String xml) {
        Objects.requireNonNull(xml, "xml");
        return parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public Document parse(InputStream inputStream) {
        try {
            return createDocumentBuilderFactory()
                    .newDocumentBuilder()
                    .parse(Objects.requireNonNull(inputStream, "inputStream"));
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new IllegalArgumentException("Unable to parse XML", exception);
        }
    }

    public Optional<String> findText(Document document, String tagName) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(tagName, "tagName");

        if (document.getElementsByTagName(tagName).getLength() == 0) {
            return Optional.empty();
        }

        return Optional.of(document.getElementsByTagName(tagName).item(0).getTextContent().trim());
    }

    public String requireText(Document document, String tagName, String errorMessage) {
        return findText(document, tagName)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalStateException(errorMessage));
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(DISALLOW_DOCTYPE_DECLARATION, true);
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
        factory.setExpandEntityReferences(false);
        return factory;
    }
}
