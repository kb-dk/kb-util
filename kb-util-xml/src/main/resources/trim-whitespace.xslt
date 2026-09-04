<!-- https://stackoverflow.com/questions/33564043/how-to-let-java-xml-transformer-output-a-xml-without-any-useless-space-or-line-b -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- copy all elements as they are -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*/text()[not(normalize-space())]" />
</xsl:stylesheet>
