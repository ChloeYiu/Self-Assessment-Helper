package com.helper.util.api;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class XmlDocumentParserTest {

    @Test
    public void parse_readsXmlDocument() {
        Document document = new XmlDocumentParser().parse("<Response><Status>Success</Status></Response>");

        assertEquals("Success", document.getElementsByTagName("Status").item(0).getTextContent());
    }

    @Test
    public void findText_returnsTrimmedTagText() {
        XmlDocumentParser parser = new XmlDocumentParser();
        Document document = parser.parse("<Response><Status> Success </Status></Response>");

        assertEquals("Success", parser.findText(document, "Status").orElseThrow());
    }

    @Test
    public void parse_keepsMultipleMatchingElementsInDocumentOrder() {
        Document document = new XmlDocumentParser().parse(
                "<Trades>"
                        + "<Trade symbol=\"ACWI\" />"
                        + "<Trade symbol=\"VUSA\" />"
                        + "<Trade symbol=\"EQQQ\" />"
                        + "</Trades>");

        assertEquals(3, document.getElementsByTagName("Trade").getLength());
        assertEquals("ACWI", document.getElementsByTagName("Trade").item(0).getAttributes()
                .getNamedItem("symbol")
                .getTextContent());
        assertEquals("VUSA", document.getElementsByTagName("Trade").item(1).getAttributes()
                .getNamedItem("symbol")
                .getTextContent());
        assertEquals("EQQQ", document.getElementsByTagName("Trade").item(2).getAttributes()
                .getNamedItem("symbol")
                .getTextContent());
    }

    @Test
    public void findText_returnsEmptyWhenTagIsMissing() {
        XmlDocumentParser parser = new XmlDocumentParser();
        Document document = parser.parse("<Response></Response>");

        assertTrue(parser.findText(document, "Status").isEmpty());
    }

    @Test
    public void requireText_rejectsMissingTag() {
        XmlDocumentParser parser = new XmlDocumentParser();
        Document document = parser.parse("<Response></Response>");

        assertThrows(
                IllegalStateException.class,
                () -> parser.requireText(document, "Status", "status is missing"));
    }

    @Test
    public void parse_rejectsDoctypeDeclaration() {
        String xml = "<!DOCTYPE Response [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<Response>&xxe;</Response>";

        assertThrows(
                IllegalArgumentException.class,
                () -> new XmlDocumentParser().parse(xml));
    }
}
