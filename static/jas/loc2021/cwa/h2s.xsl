<xsl:transform version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- THESE OUTPUT SETTINGS MAY BE OVERRIDDEN BY THE H2S PROCESSOR: -->

<xsl:output method="xml" omit-xml-declaration="yes"
    indent="yes" encoding="UTF-8"/>

<!--======== Process single sign ============-->

<!--=========================================-->
<!--######## INCLUDED TRANSFORM FILE ########-->
<!--=========================================-->


<!--========== Begin h2SignsH4.xsl ==========-->

<!-- 2002-04-08: Saving version with null-<text> elements -->


<!--==========================================-->
<!--######## INCLUDED TRANSFORM FILES ########-->
<!--==========================================-->


<!--========== Begin h2sNonManualsH4.xsl ==========-->

<!-- NB  THIS IS CURRENTLY AN UNAMBIGUOUSLY CHEAP-AND-CHEERFUL, -->
<!-- I.E. INCOMPLETE, VERSION OF THE REQUIRED TRANSFORM.  IN    -->
<!-- PARTICULAR, IT DOES NOT SUPPORT SEQUENTIAL OR PARALLEL     -->
<!-- COMPOSITION OF BASIC ELEMENTS OF A GIVEN KIND, NOR THE     -->
<!-- SYNCHRONIZATION ATTRIBUTES.    2001-12-08                  -->

<!-- ALLOWS hnm_extra ELEMENTS.     2003-10-02                  -->


<!--######## hamnosys_nonmanual ########-->
<!--====================================-->
<xsl:template match="hamnosys_nonmanual">
    <!--
    <!ELEMENT hamnosys_nonmanual (
        ( hnm_shoulder
        | hnm_body
        | hnm_head
        | hnm_eyegaze
        | hnm_eyebrows
        | hnm_eyelids
        | hnm_nose
        | hnm_mouthgesture
        | hnm_mouthpicture
        | hns_extra )+
    )>
    -->

    <xsl:element name="sign_nonmanual">

        <xsl:if test="hnm_shoulder">
            <xsl:element name="shoulder_tier">
                <xsl:choose>
                    <xsl:when test="count(hnm_shoulder)=1">
                        <xsl:apply-templates select="hnm_shoulder"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="shoulder_par">
                            <xsl:apply-templates select="hnm_shoulder"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>


        <xsl:if test="hnm_body">
            <xsl:element name="body_tier">
                <xsl:choose>
                    <xsl:when test="count(hnm_body)=1">
                        <xsl:apply-templates select="hnm_body"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="body_par">
                            <xsl:apply-templates select="hnm_body"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>

        <xsl:if test="hnm_head">
            <xsl:element name="head_tier">
                <xsl:choose>
                    <xsl:when test="count(hnm_head)=1">
                        <xsl:apply-templates select="hnm_head"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="head_par">
                            <xsl:apply-templates select="hnm_head"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>

        <xsl:if test="hnm_eyegaze">
            <xsl:element name="eyegaze_tier">
                <xsl:choose>
                    <xsl:when test="count(hnm_eyegaze)=1">
                        <xsl:apply-templates select="hnm_eyegaze"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="eye_par">
                            <xsl:apply-templates select="hnm_eyegaze"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>

        <xsl:if test="hnm_eyebrows | hnm_eyelids | hnm_nose">
            <xsl:element name="facialexpr_tier">
                <xsl:choose>
                    <xsl:when test=
                      "count(hnm_eyebrows | hnm_eyelids | hnm_nose)=1">
                        <xsl:apply-templates
                            select=
                            "hnm_eyebrows | hnm_eyelids | hnm_nose"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="facial_expr_par">
                            <xsl:apply-templates
                                select=
                                "hnm_eyebrows | hnm_eyelids | hnm_nose"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>

        <xsl:if test="hnm_mouthgesture | hnm_mouthpicture">
            <xsl:element name="mouthing_tier">
                <xsl:choose>
                    <xsl:when test=
                      "count(hnm_mouthgesture | hnm_mouthpicture)=1">
                        <xsl:apply-templates
                            select=
                            "hnm_mouthgesture | hnm_mouthpicture"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="mouthing_par">
                            <xsl:apply-templates
                                select=
                                "hnm_mouthgesture | hnm_mouthpicture"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>


        <xsl:if test="hnm_extra">
            <xsl:element name="extra_tier">
                <xsl:choose>
                    <xsl:when test="count(hnm_extra)=1">
                        <xsl:apply-templates select="hnm_extra"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="extra_par">
                            <xsl:apply-templates select="hnm_extra"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>
    </xsl:element>

</xsl:template>


<!--######## hnm_shoulder ########-->
<!--==============================-->
<xsl:template match="hnm_shoulder">
    <!--
    <!ELEMENT hnm_shoulder EMPTY>
    <!ATTLIST hnm_shoulder
        tag  ( %shoulder_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="shoulder_movement">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_body ########-->
<!--==========================-->
<xsl:template match="hnm_body">
    <!--
    <!ELEMENT hnm_body EMPTY>
    <!ATTLIST hnm_body
        tag  ( %body_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="body_movement">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_head ########-->
<!--==========================-->
<xsl:template match="hnm_head">
    <!--
    <!ELEMENT hnm_head EMPTY>
    <!ATTLIST hnm_head
        tag  ( %head_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="head_movement">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_eyegaze ########-->
<!--=============================-->
<xsl:template match="hnm_eyegaze">
    <!--
    <!ELEMENT hnm_eyegaze EMPTY>
    <!ATTLIST hnm_eyegaze
        tag  ( %eyegaze_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="eye_gaze">
        <xsl:attribute name="direction">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_eyebrows ########-->
<!--==============================-->
<xsl:template match="hnm_eyebrows">
    <!--
    <!ELEMENT hnm_eyebrows EMPTY>
    <!ATTLIST hnm_eyebrows
        tag  ( %eyebrows_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="eye_brows">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_eyelids ########-->
<!--=============================-->
<xsl:template match="hnm_eyelids">
    <!--
    <!ELEMENT hnm_eyelids EMPTY>
    <!ATTLIST hnm_eyelids
        tag  ( %eyelids_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="eye_lids">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_nose ########-->
<!--==========================-->
<xsl:template match="hnm_nose">
    <!--
    <!ELEMENT hnm_nose EMPTY>
    <!ATTLIST hnm_nose
        tag  ( %nose_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="nose">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_mouthgesture ########-->
<!--==================================-->
<xsl:template match="hnm_mouthgesture">
    <!--
    <!ELEMENT hnm_mouthgesture EMPTY>
    <!ATTLIST hnm_mouthgesture
        tag  ( %mouthgesture_tag; )  #REQUIRED
    >
    -->

    <xsl:element name="mouth_gesture">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_mouthpicture ########-->
<!--==================================-->
<xsl:template match="hnm_mouthpicture">
    <!--
    <!ELEMENT hnm_mouthpicture EMPTY>
    <!ATTLIST hnm_mouthpicture
        picture  CDATA  #REQUIRED
    >
    -->

    <xsl:element name="mouth_picture">
        <xsl:attribute name="picture">
            <xsl:value-of select="@picture"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## hnm_extra ########-->
<!--==========================-->
<xsl:template match="hnm_extra">
    <!--
    <!ELEMENT hnm_extra EMPTY>
    <!ATTLIST hnm_extra
        tag  CDATA  #REQUIRED
    >
    -->

    <xsl:element name="extra_movement">
        <xsl:attribute name="movement">
            <xsl:value-of select="@tag"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>

<!--=========== End h2sNonManualsH4.xsl ===========-->

<!--=========== Begin h2sHandConfigAttribsH4.xsl ===========-->

<!--==================================================-->
<!--######## BASIC handshape ATTRIBUTE VALUES ########-->
<!--==================================================-->


<!--######## hShapeOld2New ########-->
<!--===============================-->
<xsl:template name="hShapeOld2New">
  <xsl:param name="hs"/>

    <xsl:choose>
        <xsl:when test="$hs='ham_flathand'">
            <xsl:value-of select="'flat'"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="substring-after($hs,'ham_')"/>
        </xsl:otherwise>
    </xsl:choose>

</xsl:template>


<!--######## handShapeValue ########-->
<!--================================-->
<xsl:template name="handShapeValue">

    <xsl:call-template name="hShapeOld2New">
        <xsl:with-param name="hs" select="@handshapeclass"/>
    </xsl:call-template>
</xsl:template>


<!--######## secondHandShapeValue ########-->
<!--======================================-->
<xsl:template name="secondHandShapeValue">

    <xsl:call-template name="hShapeOld2New">
        <xsl:with-param name="hs" select="@second_handshapeclass"/>
    </xsl:call-template>
</xsl:template>

<!--
<xsl:template name="handShapeValue">
    <xsl:choose>
        <xsl:when test="@handshapeclass='ham_flathand'">
            <xsl:value-of select="'flat'"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select=
                "substring-after(@handshapeclass,'ham_')"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>
-->


<!--=====================================================-->
<!--######## FINGER BENDING ATTRIBUTE CONVERSION ########-->
<!--=====================================================-->


<!--######## fBendOld2New ########-->
<!--==============================-->
<xsl:template name="fBendOld2New">
  <xsl:param name="fb"/>

  <xsl:variable name="sfb" select="substring-after($fb,'ham_finger_')"/>
  <xsl:choose>
    <xsl:when test="$sfb='straight'">bent</xsl:when>
    <xsl:when test="$sfb='bend'">round</xsl:when>
    <xsl:otherwise><xsl:value-of select="$sfb"/></xsl:otherwise>
  </xsl:choose>

</xsl:template>


<!--######## fBendName2Codes ########-->
<!--=================================-->
<xsl:template name="fBendName2Codes">
  <xsl:param name="fb"/>

  <xsl:choose>
    <xsl:when test="$fb=''">000</xsl:when>
    <xsl:when test="$fb='bent'">400</xsl:when>
    <xsl:when test="$fb='round'">222</xsl:when>
    <xsl:when test="$fb='hooked'">044</xsl:when>
    <xsl:when test="$fb='halfbent'">200</xsl:when>
    <xsl:when test="$fb='dblbent'">440</xsl:when>
    <xsl:when test="$fb='dblhooked'">444</xsl:when>
  </xsl:choose>

</xsl:template>


<!--######## fBend1DigitMerge ########-->
<!--==================================-->
<xsl:template name="fBend1DigitMerge">
  <xsl:param name="da"/>
  <xsl:param name="db"/>

  <xsl:variable name="sum" select="$da + $db"/>

  <xsl:value-of select="($sum - ($sum mod 2)) div 2"/>

</xsl:template>


<!--######## fBendDigitsMerge ########-->
<!--==================================-->
<xsl:template name="fBendDigitsMerge">
  <xsl:param name="dda"/>
  <xsl:param name="ddb"/>

  <xsl:variable name="d1">
    <xsl:call-template name="fBend1DigitMerge">
      <xsl:with-param name="da" select="substring($dda,1,1)"/>
      <xsl:with-param name="db" select="substring($ddb,1,1)"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="d2">
    <xsl:call-template name="fBend1DigitMerge">
      <xsl:with-param name="da" select="substring($dda,2,1)"/>
      <xsl:with-param name="db" select="substring($ddb,2,1)"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="d3">
    <xsl:call-template name="fBend1DigitMerge">
      <xsl:with-param name="da" select="substring($dda,3,1)"/>
      <xsl:with-param name="db" select="substring($ddb,3,1)"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:value-of select="concat($d1,$d2,$d3)"/>

</xsl:template>


<!--######## fBendPair2Codes ########-->
<!--=================================-->
<xsl:template name="fBendPair2Codes">
    <xsl:param name="fba"/>
    <xsl:param name="fbb"/>

    <xsl:variable name="fbacodes">
        <xsl:call-template name="fBendName2Codes">
            <xsl:with-param name="fb" select="$fba"/>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="fbbcodes">
        <xsl:call-template name="fBendName2Codes">
            <xsl:with-param name="fb" select="$fbb"/>
        </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name="fBendDigitsMerge">
        <xsl:with-param name="dda" select="$fbacodes"/>
        <xsl:with-param name="ddb" select="$fbbcodes"/>
    </xsl:call-template>

</xsl:template>


<!--================================================================-->
<!--######## GENERATING SIGML HANDSHAPE ATTRIBUTES FROM HML ########-->
<!--================================================================-->


<!--######## setMainBendAttrib ########-->
<!--===================================-->
<xsl:template name="setMainBendAttrib">

    <xsl:if test="@fingerbending">
        <xsl:attribute name="mainbend">
            <xsl:call-template name='fBendOld2New'>
                <xsl:with-param name="fb"
                    select="string(@fingerbending)"/>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:if>
<!--
-->

</xsl:template>


<!--######## thumbPosOld2New ########-->
<!--=================================-->
<xsl:template name="thumbPosOld2New">
  <xsl:param name="oldthp"/>

    <xsl:variable name="thp"
            select="substring-after($oldthp,'ham_thumb_')"/>
    <xsl:choose>
        <xsl:when test="$thp='open'">opposed</xsl:when>
        <xsl:otherwise><xsl:value-of select="$thp"/></xsl:otherwise>
    </xsl:choose>

</xsl:template>


<!--######## thumbPos2CeeOpening ########-->
<!--=====================================-->
<xsl:template name="thumbPos2CeeOpening">
  <xsl:param name="thp"/>

    <!-- $thp IS EXPECTED TO HAVE ONLY TWO POSSIBLE VALUES HERE -->
    <xsl:choose>
        <xsl:when test="$thp='across'">tight</xsl:when>
        <xsl:when test="$thp='opposed'">slack</xsl:when>
    </xsl:choose>

</xsl:template>


<!--######## thumbposValue ########-->
<!--===============================-->
<xsl:template name="thumbposValue">

    <xsl:call-template name="thumbPosOld2New">
        <xsl:with-param name="oldthp" select="@thumbpos"/>
    </xsl:call-template>
<!--
    <xsl:variable name="thp" select=
        "substring-after(@thumbpos,'ham_thumb_')"/>
    <xsl:choose>
        <xsl:when test="$thp='open'">opposed</xsl:when>
        <xsl:otherwise><xsl:value-of select="$thp"/></xsl:otherwise>
    </xsl:choose>
-->
</xsl:template>


<!--######## secondThumbposValue ########-->
<!--=====================================-->
<xsl:template name="secondThumbposValue">

    <xsl:call-template name="thumbPosOld2New">
        <xsl:with-param name="oldthp" select="@second_thumbpos"/>
    </xsl:call-template>
</xsl:template>


<!--######## setThumbposAttrib ########-->
<!--===================================-->
<xsl:template name="setThumbposAttrib">

    <xsl:if test="@thumbpos">
        <xsl:attribute name="thumbpos">
            <xsl:call-template name="thumbposValue"/>
        </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## setCeeopeningAttrib ########-->
<!--=====================================-->
<xsl:template name="setCeeopeningAttrib">

    <xsl:if test="@thumbpos">
        <xsl:variable name="tp">
            <xsl:call-template name="thumbposValue"/>
        </xsl:variable>
        <xsl:if test="$tp!='out'">
            <xsl:attribute name="ceeopening">
                <xsl:call-template name="thumbPos2CeeOpening">
                    <xsl:with-param name="thp" select="$tp"/>
                </xsl:call-template>
                <!--
                <xsl:choose>
                    <xsl:when test="$tp='across'">tight</xsl:when>
                    <xsl:when test="$tp='opposed'">slack</xsl:when>
                </xsl:choose>
                -->
            </xsl:attribute>
        </xsl:if>
    </xsl:if>

</xsl:template>


<!--######## checkExemptedDigits ########-->
<!--=====================================-->
<xsl:template name="checkExemptedDigits">

    <xsl:if test="@fingerbending | @second_fingerbending">
        <xsl:if test="fingernothumb | thumbspecial/thumb">
            <xsl:attribute name="exempteddigits">
                <xsl:if test="thumbspecial/thumb">1</xsl:if>
                <xsl:apply-templates select="fingernothumb"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:if>

</xsl:template>


<!--######## checkExemptedThumbOnly ########-->
<!--========================================-->
<xsl:template name="checkExemptedThumbOnly">

    <xsl:if test="@fingerbending | @second_fingerbending">
        <xsl:if test="thumbspecial/thumb">
            <xsl:attribute name="exempteddigits">
                <xsl:value-of select="1"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:if>

</xsl:template>


<!--######## setFingersAttrib ########-->
<!--==================================-->
<xsl:template name="setFingersAttrib">
    <xsl:param name="aname"/>

    <xsl:if test="fingernothumb">
        <xsl:attribute name="{$aname}">
            <xsl:apply-templates select="fingernothumb"/>
        </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## checkOpposedFingerAttrib ########-->
<!--==================================-->
<xsl:template name="checkOpposedFingerAttrib">

    <xsl:if test="fingernothumb[@thumbopp='true']">
        <xsl:attribute name="opposedfinger">
            <xsl:apply-templates
                select="fingernothumb[@thumbopp='true']"/>
        </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## setCombiningfingersAttribs ########-->
<!--===========================================-->
<xsl:template name="setCombiningfingersAttribs">
    <xsl:param name="hs"/>

    <xsl:if test="$hs!='pinchall' and $hs!='ceeall'">
        <xsl:call-template name="setFingersAttrib">
            <xsl:with-param name="aname" select="'specialfingers'"/>
        </xsl:call-template>
        <xsl:call-template name="checkOpposedFingerAttrib"/>
    </xsl:if>

</xsl:template>


<!--######## setContactPairAttribs ########-->
<!--=======================================-->
<xsl:template name="setContactPairAttribs">

    <xsl:if test="fingercrossing">
        <xsl:apply-templates select="fingercrossing[1]">
            <xsl:with-param name="tag" select="''"/>
        </xsl:apply-templates>
        <xsl:if test="fingercrossing[2]">
            <xsl:apply-templates select="fingercrossing[2]">
                <xsl:with-param name="tag" select="'second_'"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:if>

</xsl:template>


<!--######## setThumbContactAttrib ########-->
<!--=======================================-->
<xsl:template name="setThumbContactAttrib">

    <xsl:if test="thumbspecial/fingerpart">
        <xsl:apply-templates select="thumbspecial"/>
    </xsl:if>

</xsl:template>


<!--######## setThumbSpecialAttribs ########-->
<!--========================================-->
<xsl:template name="setThumbSpecialAttribs">

    <xsl:if test="thumbspecial">
       <xsl:if test="not(thumbspecial/thumb)">
           <xsl:apply-templates select="thumbspecial"/>
       </xsl:if>
    </xsl:if>

</xsl:template>


<!--######## checkApproxHSAttrib ########-->
<!--===============================-->
<xsl:template name="checkApproxHSAttrib">
    <xsl:if test="@approx_shape='true'">
       <xsl:attribute name="approx_shape">true</xsl:attribute>
    </xsl:if>
</xsl:template>


<!--=======================================================-->
<!--######## HANDSHAPE-DEPENDENT ATTRIBUTE SETTING ########-->
<!--=======================================================-->


<!--######## doFistHSAttribs ########-->
<!--=================================-->
<xsl:template name="doFistHSAttribs">

<!--####################
2003-08
Allow more attributes on a fist than hitherto
- main-bend, exempted-digits.
ViSiCAST D5-1 indicates we must do this for  HNS-4.
-->
    <!-- fingerbending ATTRIBUTE -->
    <xsl:call-template name="setMainBendAttrib"/>

    <!-- thumbpos ATTRIBUTE -->
    <xsl:call-template name="setThumbposAttrib"/>

    <!-- fingernothumb AND thumbspecial/thumb ELEMENTS -->
    <!-- (Do we need to exclude thumbspecial/thumb here?) -->
    <xsl:call-template name="checkExemptedDigits"/>

    <!-- fingershape ELEMENTS -> EXPLICIT bendN ATTRIBUTES -->
    <xsl:apply-templates select="fingershape"/>

    <!-- fingercrossing ELEMENTS -> contact ATTRIBUTES -->
    <xsl:call-template name="setContactPairAttribs"/>

    <!-- thumbspecial[not(thumb)] ELEMENT -->
    <xsl:call-template name="setThumbSpecialAttribs"/>

</xsl:template>


<!--######## doFlatHSAttribs ########-->
<!--=================================-->
<xsl:template name="doFlatHSAttribs">

    <!-- fingerbending ATTRIBUTE -->
    <xsl:call-template name="setMainBendAttrib"/>
    <!--
        <xsl:with-param name="baname" select="'mainbend'"/>
    </xsl:call-template>
    -->

    <!-- thumbpos ATTRIBUTE -->
    <xsl:call-template name="setThumbposAttrib"/>

    <!-- fingernothumb AND thumbspecial/thumb ELEMENTS -->
    <xsl:call-template name="checkExemptedDigits"/>

    <!-- fingershape ELEMENTS -> EXPLICIT bendN ATTRIBUTES -->
    <xsl:apply-templates select="fingershape"/>

    <!-- fingercrossing ELEMENTS -> contact ATTRIBUTES -->
    <xsl:call-template name="setContactPairAttribs"/>

    <!-- thumbspecial[not(thumb)] ELEMENT -->
    <xsl:call-template name="setThumbSpecialAttribs"/>

</xsl:template>


<!--######## doFinger2HSAttribs ########-->
<!--====================================-->
<xsl:template name="doFinger2HSAttribs">

    <!-- fingerbending ATTRIBUTE -->
    <xsl:call-template name="setMainBendAttrib"/>

    <!-- thumbpos ATTRIBUTE -->
    <xsl:call-template name="setThumbposAttrib"/>

    <!-- fingernothumb AND thumbspecial/thumb ELEMENTS -->
    <xsl:if test="count(fingernothumb)=1">
        <xsl:call-template name="setFingersAttrib">
            <xsl:with-param name="aname" select="'specialfingers'"/>
        </xsl:call-template>
    </xsl:if>
    <xsl:call-template name="checkExemptedThumbOnly"/>

    <!-- fingershape ELEMENTS -> EXPLICIT bendN ATTRIBUTES -->
    <xsl:apply-templates select="fingershape"/>

    <!-- fingercrossing ELEMENTS -> contact ATTRIBUTES -->
    <xsl:call-template name="setContactPairAttribs"/>

    <!-- thumbspecial[not(thumb)] ELEMENT -->
    <xsl:call-template name="setThumbSpecialAttribs"/>

</xsl:template>


<!--######## doFinger23HSAttribs ########-->
<!--=====================================-->
<xsl:template name="doFinger23HSAttribs">

    <!-- fingerbending ATTRIBUTE -->
    <xsl:call-template name="setMainBendAttrib"/>

    <!-- thumbpos ATTRIBUTE -->
    <xsl:call-template name="setThumbposAttrib"/>

    <!-- fingernothumb AND thumbspecial/thumb ELEMENTS -->
    <xsl:choose>
        <xsl:when test="count(fingernothumb) &lt; 2">
            <xsl:call-template name="checkExemptedDigits"/>
        </xsl:when>
        <xsl:when test="count(fingernothumb) &gt;= 2">
            <xsl:call-template name="setFingersAttrib">
                <xsl:with-param name="aname" select="'specialfingers'"/>
            </xsl:call-template>
            <xsl:call-template name="checkExemptedThumbOnly"/>
        </xsl:when>
    </xsl:choose>

    <!-- fingershape ELEMENTS -> EXPLICIT bendN ATTRIBUTES -->
    <xsl:apply-templates select="fingershape"/>

    <!-- fingercrossing ELEMENTS -> contact ATTRIBUTES -->
    <xsl:call-template name="setContactPairAttribs"/>

    <!-- thumbspecial[not(thumb)] ELEMENT -->
    <xsl:call-template name="setThumbSpecialAttribs"/>

</xsl:template>


<!--######## doPinchHSAttribs ########-->
<!--==================================-->
<xsl:template name="doPinchHSAttribs">
    <xsl:param name="hs"/>

    <!-- fingerbending ATTRIBUTE -->
    <xsl:call-template name="setMainBendAttrib"/>

    <!-- fingernothumb ELEMENTS -->
    <xsl:call-template name="setCombiningfingersAttribs">
        <xsl:with-param name="hs" select="$hs"/>
    </xsl:call-template>
    <!-- ... or for exemption from a mainBend setting -->
    <xsl:call-template name="checkExemptedDigits"/>

    <!-- fingershape ELEMENTS -> EXPLICIT bendN ATTRIBUTES -->
    <xsl:apply-templates select="fingershape"/>

    <!-- fingercrossing ELEMENTS -> contact ATTRIBUTES -->
    <xsl:call-template name="setContactPairAttribs"/>

    <!-- fingerpart IS THE ONLY PERMITTED thumbspecial ELEMENT -->
    <xsl:call-template name="setThumbContactAttrib"/>

</xsl:template>


<!--######## doCeeHSAttribs ########-->
<!--================================-->
<xsl:template name="doCeeHSAttribs">
    <xsl:param name="hs"/>

    <!-- fingerbending ATTRIBUTE -->
    <xsl:call-template name="setMainBendAttrib"/>

    <!-- thumbpos ATTRIBUTE -->
    <xsl:call-template name="setCeeopeningAttrib"/>

    <!-- fingernothumb ELEMENTS -->
    <xsl:call-template name="setCombiningfingersAttribs">
        <xsl:with-param name="hs" select="$hs"/>
    </xsl:call-template>

    <!-- fingershape ELEMENTS -> EXPLICIT bendN ATTRIBUTES -->
    <xsl:apply-templates select="fingershape"/>

    <!-- fingercrossing ELEMENTS -> contact ATTRIBUTES -->
    <xsl:call-template name="setContactPairAttribs"/>

</xsl:template>

<!--=========== End h2sHandConfigAttribsH4.xsl ===========-->

<!--=========== Begin h2sMoveAttribsH4.xsl ===========-->

<!--
pairOf18sTo26 is redundant (since the change to straight/circular
movement handling), 2003-08.
-->
<!--######## pairOf18sTo26 ########-->
<!--===============================-->
<!--
<xsl:template name="pairOf18sTo26">

    <xsl:param name="stra"/>
    <xsl:param name="strb"/>

    <xsl:choose>

        <xsl:when test="string-length($stra)=2 and string-length($strb)=2">
            <xsl:choose>
                <xsl:when test="substring($stra,2,1)=substring($strb,1,1)">
                    <xsl:value-of
                        select="concat($stra,substring($strb,2,1))"/>
                </xsl:when>
                <xsl:when test="substring($strb,2,1)=substring($stra,1,1)">
                    <xsl:value-of
                        select="concat($strb,substring($stra,2,1))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$stra"/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:when>

        <xsl:otherwise>
            <xsl:value-of select="$stra"/>
        </xsl:otherwise>

    </xsl:choose>

</xsl:template>
-->


<!--######## checkBouncing ########-->
<!--===============================-->
<xsl:template name="checkBouncing">
    <xsl:if test="ancestor::action1[1]/@bouncing='true'">
       <xsl:attribute name="bouncing">true</xsl:attribute>
    </xsl:if>
</xsl:template>


<!--######## checkCurrentAlternating ########-->
<!--=========================================-->
<xsl:template name="checkCurrentAlternating">

    <xsl:if test="@alternating='true'">
       <xsl:attribute name="alternating">true</xsl:attribute>
         <xsl:if test="@second_alternating='true'">
           <xsl:attribute name="second_alternating">true</xsl:attribute>
        </xsl:if>
    </xsl:if>

</xsl:template>


<!--######## checkAncestorAlternating ########-->
<!--==========================================-->
<xsl:template name="checkAncestorAlternating">

    <xsl:variable name="ancstrtgt" select=
        "ancestor-or-self::*[name()='action1t' or name()='action2t'][1]"/>

    <!-- THIS IS THE POINT WHERE WE ENGAGE IN GROSS HACKERY IN -->
    <!-- ORDER TO DETERMINE WHETHER OR NOT SOME ANCESTOR HAS   -->
    <!-- ALREADY GENERATED ANY NECESSARY alternating           -->
    <!-- ATTRIBUTES.                                           -->

    <xsl:if test=
            "$ancstrtgt/@alternating='true'  and
             not($ancstrtgt/@repetition)  and
             ( count($ancstrtgt/*)=1  or
               count($ancstrtgt/action2/*)=1 )">

        <xsl:attribute name="alternating">true</xsl:attribute>
        <xsl:if test="$ancstrtgt/@second_alternating='true'">
            <xsl:attribute name="second_alternating">true</xsl:attribute>
        </xsl:if>

    </xsl:if>

</xsl:template>


<!--######## convertSize ########-->
<!--=============================-->
<xsl:template name="convertSize">
    <xsl:param name="sz"/>
    <xsl:choose>
        <xsl:when test="$sz='large'">big</xsl:when>
        <xsl:otherwise><xsl:value-of select="$sz"/></xsl:otherwise>
    </xsl:choose>
</xsl:template>


<!--######## rptValue ########-->
<!--==========================-->
<xsl:template name="rptValue">
    <xsl:param name="r"/>
    <xsl:choose>
        <xsl:when test="$r='fromstartseveral'">fromstart_several</xsl:when>
        <xsl:when test="$r='continueseveral'">continue_several</xsl:when>
        <xsl:otherwise><xsl:value-of select="$r"/></xsl:otherwise>
    </xsl:choose>
</xsl:template>


<!--######## setMainRepetitionAttribs ########-->
<!--==========================================-->
<xsl:template name="setMainRepetitionAttribs">
    <!--
    <!ATTLIST action?t
        repetition ( %repetition; ) #IMPLIED
        second_repetition ( %repetition; ) #IMPLIED
        repetition_incrdecr ( %incrdecr; ) #IMPLIED
        repetition_incrdecr_size ( %size; ) #IMPLIED
        repetition_baseshift ( %movementarrow; ) #IMPLIED
        baseshift_size ( %size; ) #IMPLIED
        baseshift_incrdecr ( %incrdecr; ) #IMPLIED
        baseshift_incrdecr_size ( %size; ) #IMPLIED
        approx_repetition  %boolfalse;
        ...
    >
    2011-04
    ADDED approx_repetition ATTRIBUTE  - MAY POSSIBLY APPEAR
    WHEN repetition='fromstartseveral' BUT NOT OTHERWISE.
    -->

    <xsl:attribute name="repetition">
        <xsl:call-template name="rptValue">
            <xsl:with-param name="r" select=
                "substring-after(@repetition,'ham_repeat_')"/>
        </xsl:call-template>
    </xsl:attribute>

    <xsl:if test="@second_repetition">
        <xsl:attribute name="second_repetition">
            <xsl:call-template name="rptValue">
                <xsl:with-param name="r" select=
                    "substring-after(@second_repetition,'ham_repeat_')"/>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="@repetition_incrdecr">
        <xsl:attribute name="repetition_incrdecr">
            <xsl:value-of select=
                "substring-after(@repetition_incrdecr,'ham_')"/>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="@repetition_incrdecr_size">
      <xsl:attribute name="repetition_incrdecr_size">
        <xsl:call-template name="convertSize">
          <xsl:with-param name="sz" select="@repetition_incrdecr_size"/>
        </xsl:call-template>
      </xsl:attribute>
    </xsl:if>

    <xsl:if test="@repetition_baseshift">
        <xsl:attribute name="repetition_baseshift">
            <xsl:value-of select=
                "substring-after(@repetition_baseshift,'ham_move_')"/>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="@baseshift_size">
      <xsl:attribute name="baseshift_size">
        <xsl:call-template name="convertSize">
          <xsl:with-param name="sz" select="@baseshift_size"/>
        </xsl:call-template>
      </xsl:attribute>
    </xsl:if>

    <xsl:if test="@baseshift_incrdecr">
        <xsl:attribute name="baseshift_incrdecr">
            <xsl:value-of select=
                "substring-after(@baseshift_incrdecr,'ham_')"/>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="@baseshift_incrdecr_size">
      <xsl:attribute name="baseshift_incrdecr_size">
        <xsl:call-template name="convertSize">
          <xsl:with-param name="sz" select="@baseshift_incrdecr_size"/>
        </xsl:call-template>
      </xsl:attribute>
    </xsl:if>

    <xsl:if test="@approx_repetition='true'">
       <xsl:attribute name="approx_repetition">true</xsl:attribute>
       <xsl:call-template name="copyEllipsedirectionAttrib"/>
    </xsl:if>

</xsl:template>


<!--######## copySimpleParentAttribs ########-->
<!--=========================================-->
<xsl:template name="copySimpleParentAttribs">

    <xsl:if test="../@modifier">
        <xsl:attribute name="manner">
            <xsl:value-of select="substring-after(../@modifier,'ham_')"/>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="../@ham_fast='true'">
        <xsl:attribute name="fast">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="../@ham_slow='true'">
        <xsl:attribute name="slow">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="../@ham_tense='true'">
        <xsl:attribute name="tense">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="../@ham_rest='true'">
        <xsl:attribute name="rest">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="../@ham_halt='true'">
        <xsl:attribute name="halt">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="../@def_locname">
        <xsl:attribute name="def_locname">
            <xsl:value-of
                select="substring-after(../@def_locname,'loc')"/>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="../@bouncing='true'">
        <xsl:attribute name="bouncing">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="../@abs_motion='true'">
        <xsl:attribute name="abs_motion">true</xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## copySizeAttrib ########-->
<!--================================-->
<xsl:template name="copySizeAttrib">

    <xsl:if test="@size">
      <xsl:attribute name="size">
        <xsl:call-template name="convertSize">
          <xsl:with-param name="sz" select="@size"/>
        </xsl:call-template>
      </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## copyDynamicsizeAttribs ########-->
<!--========================================-->
<xsl:template name="copyDynamicsizeAttribs">

    <xsl:if test="@incrdecr">
      <xsl:attribute name="incrdecr">
        <xsl:value-of select="substring-after(@incrdecr,'ham_')"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@incrdecr_size">
      <xsl:attribute name="incrdecr_size">
        <xsl:call-template name="convertSize">
          <xsl:with-param name="sz" select="@incrdecr_size"/>
        </xsl:call-template>
      </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## copyEllipsedirectionAttrib ########-->
<!--============================================-->
<xsl:template name="copyEllipsedirectionAttrib">

    <xsl:if test="@ellipsedirection">
      <xsl:attribute name="ellipse_direction">
        <xsl:value-of select=
          "substring-after(@ellipsedirection,'ham_ellipse_')"/>
      </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## copyZigzagAttribs ########-->
<!--===================================-->
<xsl:template name="copyZigzagAttribs">

    <xsl:if test="@zigzagstyle">
      <xsl:attribute name="zigzag_style">
        <xsl:value-of select="substring-after(@zigzagstyle,'ham_')"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@zigzag_size">
      <xsl:attribute name="zigzag_size">
        <xsl:call-template name="convertSize">
          <xsl:with-param name="sz" select="@zigzag_size"/>
        </xsl:call-template>
      </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## copyClockAttribs ########-->
<!--==================================-->
<xsl:template name="copyClockAttribs">

    <xsl:if test="@start">
      <xsl:attribute name="start">
        <xsl:value-of select="substring-after(@start,'ham_clock_')"/>
      </xsl:attribute>
    </xsl:if>

    <xsl:if test="@clockfull='true'">
      <xsl:attribute name="clockplus">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="@second_clockfull='true'">
      <xsl:attribute name="second_clockplus">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="@end">
      <xsl:attribute name="end">
        <xsl:value-of select="substring-after(@end,'ham_clock_')"/>
      </xsl:attribute>
    </xsl:if>

</xsl:template>

<!--=========== End h2sMoveAttribsH4.xsl ===========-->


<!--===================================-->
<!--######## UTILITY FUNCTIONS ########-->
<!--===================================-->


<!--######## checkTrueAttrib ########-->
<!--=================================-->
<xsl:template name="checkTrueAttrib">
    <xsl:param name="aname"/>

    <xsl:variable name="anode" select="@*[name()=$aname]"/>
    <xsl:if test="$anode='true'">
        <xsl:attribute name="{$aname}">true</xsl:attribute>
    </xsl:if>
</xsl:template>


<!--================================-->
<!--######## SIGN STRUCTURE ########-->
<!--================================-->


<!--######## hamnosysml ########-->
<!--============================-->
<xsl:template match="hamnosysml">
    <!-- <!ELEMENT hamnosysml (avatar?, sign*)> -->

    <xsl:element name="sigml">
        <!-- IGNORE avatar? FOR NOW -->
        <xsl:apply-templates select="sign | comment()"/>
    </xsl:element>

</xsl:template>


<!--######## sign ########-->
<!--======================-->
<xsl:template match="sign">
    <!--
    <!ELEMENT sign (hamnosys_sign?)>
    <!ATTLIST sign gloss CDATA #IMPLIED>
    -->

    <xsl:element name="hamgestural_sign">

        <xsl:if test="@gloss">
            <xsl:attribute name="gloss">
                <xsl:value-of select="@gloss"/>
            </xsl:attribute>
        </xsl:if>

        <!-- 2010-01: Also copy duration, speed, timescale attributes. -->

		<xsl:if test="@duration">
            <xsl:attribute name="duration">
                <xsl:value-of select="@duration"/>
            </xsl:attribute>
        </xsl:if>

        <xsl:if test="@speed">
            <xsl:attribute name="speed">
                <xsl:value-of select="@speed"/>
            </xsl:attribute>
        </xsl:if>

        <xsl:if test="@timescale">
            <xsl:attribute name="timescale">
                <xsl:value-of select="@timescale"/>
            </xsl:attribute>
        </xsl:if>

        <xsl:apply-templates select="*"/>

    </xsl:element>

</xsl:template>


<!--######## hamnosys_sign ########-->
<!--===============================-->
<xsl:template match="hamnosys_sign">
    <!--
    <!ELEMENT hamnosys_sign (
        hamnosys_nonmanual?, (sign2 | sign1)*
    )>
    -->

    <!-- SEE h2sNonManualsH4.xsl FOR HANDLING OF NON-MANUALS -->

    <xsl:apply-templates select="hamnosys_nonmanual"/>

    <!-- WRAP sign? CHILDREN IF AND ONLY IF THEIR NUMBER IS NOT ONE -->

    <xsl:choose>

        <xsl:when test="count(sign2 | sign1)=1">
            <xsl:apply-templates select="sign2 | sign1"/>
        </xsl:when>

        <xsl:when test="count(sign2 | sign1)!=1">
            <xsl:element name="sign_manual">
                <xsl:apply-templates select="sign2 | sign1"/>
            </xsl:element>
        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## sign2 ########-->
<!--=======================-->
<xsl:template match="sign2">
    <!--
    <!ELEMENT sign2 (
        (nminitialconfig?, (minitialconfig2 | minitialconfig1), action1t*)
      | (symmoperator, nminitialconfig?, minitialconfig2, action2t*)
    )>
    <!ATTLIST sign2
        nondominant   %boolfalse;
        holdover      %boolfalse;
    >

    2004-06-09:
    Introduce the minitialconfig1 sub-option above, to go with the
    holdodver attribute; also introduce the nondominant attribute.
    -->

    <xsl:element name="sign_manual">
        <xsl:call-template name="checkTrueAttrib">
            <xsl:with-param name="aname" select="'nondominant'"/>
        </xsl:call-template>
        <xsl:call-template name="checkTrueAttrib">
            <xsl:with-param name="aname" select="'holdover'"/>
        </xsl:call-template>
        <xsl:apply-templates select="*"/>
    </xsl:element>

</xsl:template>


<!--######## sign1 ########-->
<!--=======================-->
<xsl:template match="sign1">
    <!--
    <!ELEMENT sign1 (nminitialconfig?, minitialconfig1, action1t*)>
    <!ATTLIST sign1 which_hand ( nondominant ) #IMPLIED>
    -->

    <xsl:element name="sign_manual">
        <xsl:if test="@which_hand='nondominant'">
            <xsl:attribute name='nondominant'>true</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="*"/>
    </xsl:element>

</xsl:template>


<!--######## symmoperator ########-->
<!--==============================-->
<xsl:template match="symmoperator">
    <!--
    <!ELEMENT symmoperator EMPTY>
    <!ATTLIST symmoperator
        att_par_or_lr (hamsymmpar | hamsymmlr) #REQUIRED
        attrib_oi_symm (hamfingerbent) #IMPLIED
        attrib_ud_symm (hamlargemod) #IMPLIED
        outofphase %boolfalse;
    >
    -->

    <!-- GENERATE ATTRIBUTES FOR PARENT sign_manual ELEMENT -->

    <xsl:attribute name="both_hands">true</xsl:attribute>

    <xsl:if test="@att_par_or_lr='hamsymmlr'">
        <xsl:attribute name="lr_symm">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="@attrib_ud_symm">
        <xsl:attribute name="ud_symm">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="@attrib_oi_symm">
        <xsl:attribute name="oi_symm">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="@outofphase='true'">
        <xsl:attribute name="outofphase">true</xsl:attribute>
    </xsl:if>

    <xsl:if test="@nonipsi='true'">
        <xsl:attribute name="realspace">true</xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## nminitialconfig ########-->
<!--=================================-->
<xsl:template match="nminitialconfig">
    <!--
    <!ELEMENT nminitialconfig (((levelbody | levelarm), action1)+)>
    -->

    <!-- SYNTACTICALLY, IF NOT SEMANTICALLY, THIS -->
    <!-- QUALIFIES AS HAND CONFIGURATION.         -->

    <xsl:call-template name="nmInitConfigListAsPairs">
        <xsl:with-param name="i" select="1"/>
        <xsl:with-param name="n" select="count(*)"/>
    </xsl:call-template>

</xsl:template>


<xsl:template name="nmInitConfigListAsPairs">
    <xsl:param name="i"/>
    <xsl:param name="n"/>

    <!-- ELEMENT FOR FIRST PAIR OF CHILDREN -->
    <xsl:element name="nonmanualconfig">
        <xsl:apply-templates select="*[$i]" mode="inNonManContext"/>
        <xsl:apply-templates select="*[$i+1]"/>
    </xsl:element>

    <xsl:if test="$i+2 &lt; $n">
        <!-- RECURSIVE CALL FOR REMAINING PAIRS OF CHILDREN -->
        <xsl:call-template name="nmInitConfigListAsPairs">
            <xsl:with-param name="i" select="$i+2"/>
            <xsl:with-param name="n" select="$n"/>
        </xsl:call-template>
    </xsl:if>

</xsl:template>


<!--====================================-->
<!--######## HAND CONFIGURATION ########-->
<!--====================================-->


<!--######## minitialconfig2 ########-->
<!--=================================-->
<xsl:template match="minitialconfig2">
    <!-- <!ELEMENT minitialconfig2 (handconfig2, location2?)> -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## minitialconfig1 ########-->
<!--=================================-->
<xsl:template match="minitialconfig1">
    <!-- minitialconfig1 (handconfig1, location1?) -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## handconfig2 ########-->
<!--######## extfidir2 ########-->
<!--######## palmor2 ########-->
<!--=============================-->
<xsl:template match="handconfig2">
    <!-- <!ELEMENT handconfig2 (
          (handshape2, extfidir2?, palmor2?)
          | (handconfig1, handconfig1)
          )>
    -->
    <!-- <!ELEMENT extfidir2 (extfidir1, extfidir1?)> -->
    <!-- <!ELEMENT palmor2 (palmor1, palmor1?)> -->

    <xsl:choose>
        <xsl:when test="handshape2">
            <xsl:apply-templates select="." mode="hsfirst"/>
        </xsl:when>
        <xsl:when test="handconfig1">
            <xsl:apply-templates select="." mode="split"/>
        </xsl:when>
    </xsl:choose>
</xsl:template>


<xsl:template match="handconfig2" mode="split">

    <!-- Hand-shapes: -->
    <xsl:choose>

        <!-- Single handconfig1 - no splitting: -->
        <xsl:when test="count(*)=1">
            <xsl:apply-templates select="*"/>
        </xsl:when>

        <!-- Double handconfig1 - each child is split: -->
        <xsl:when test="count(*)=2">
            <xsl:element name="split_handconfig">
                <xsl:apply-templates select="*/handshape1"/>
            </xsl:element>

            <xsl:if test="*/extfidir1 | */palmor1">
                <xsl:element name="split_handconfig">
                    <xsl:element name="handconfig">
                        <xsl:apply-templates
                            select="handconfig1[1]/extfidir1"/>
                        <xsl:apply-templates
                            select="handconfig1[1]/palmor1"/>
                        <xsl:text/>
                    </xsl:element>
                    <xsl:element name="handconfig">
                        <xsl:apply-templates
                            select="handconfig1[2]/extfidir1"/>
                        <xsl:apply-templates
                            select="handconfig1[2]/palmor1"/>
                        <xsl:text/>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:when>
    </xsl:choose>

</xsl:template>


<xsl:template match="handconfig2" mode="hsfirst">

    <xsl:apply-templates select="handshape2"/>

    <xsl:choose>

        <!-- BOTH SPLIT: -->
        <xsl:when test="count(extfidir2/*)=2 and count(palmor2/*)=2">
            <xsl:element name="split_handconfig">
                <xsl:element name="handconfig">
                    <xsl:apply-templates select="extfidir2/*[1]"/>
                    <xsl:apply-templates select="palmor2/*[1]"/>
                    <xsl:text/>
                </xsl:element>
                <xsl:element name="handconfig">
                    <xsl:apply-templates select="extfidir2/*[2]"/>
                    <xsl:apply-templates select="palmor2/*[2]"/>
                    <xsl:text/>
                </xsl:element>
            </xsl:element>
        </xsl:when>

        <!-- extfidir SPLIT, palmor SINGLE: -->
        <xsl:when test="count(extfidir2/*)=2">
            <xsl:element name="split_handconfig">
                <xsl:element name="handconfig">
                    <xsl:apply-templates select="extfidir2/extfidir1[1]"/>
                    <xsl:text/>
                </xsl:element>
                <xsl:element name="handconfig">
                    <xsl:apply-templates select="extfidir2/extfidir1[2]"/>
                    <xsl:text/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="handconfig">
                <xsl:apply-templates select="palmor2/*"/>
                <xsl:text/>
            </xsl:element>
        </xsl:when>

        <!-- extfidir SINGLE, palmor SPLIT: -->
        <xsl:when test="count(palmor2/*)=2">
            <xsl:element name="handconfig">
                <xsl:apply-templates select="extfidir2/*"/>
                <xsl:text/>
            </xsl:element>
            <xsl:element name="split_handconfig">
                <xsl:element name="handconfig">
                    <xsl:apply-templates select="palmor2/palmor1[1]"/>
                    <xsl:text/>
                </xsl:element>
                <xsl:element name="handconfig">
                    <xsl:apply-templates select="palmor2/palmor1[2]"/>
                    <xsl:text/>
                </xsl:element>
            </xsl:element>
        </xsl:when>

        <!-- BOTH SINGLE: -->
        <xsl:otherwise>
            <xsl:element name="handconfig">
                <xsl:apply-templates select="extfidir2/*"/>
                <xsl:text/>
            </xsl:element>
            <xsl:element name="handconfig">
                <xsl:apply-templates select="palmor2/*"/>
                <xsl:text/>
            </xsl:element>
        </xsl:otherwise>

    </xsl:choose>

</xsl:template>


<!--######## handconfig1 ########-->
<!--=============================-->
<xsl:template match="handconfig1">
    <!-- <!ELEMENT handconfig1 (handshape1, extfidir1?, palmor1?)> -->

    <!-- WE COULD WRAP ALL THE CONTENT UP IN A SINGLE ELEMENT, -->
    <!-- BUT WE LEAVE THAT UNTIL LATER -->

    <xsl:apply-templates select="handshape1"/>

    <xsl:element name="handconfig">
        <xsl:apply-templates select="*[not(self::handshape1)]"/>
        <xsl:text/>
    </xsl:element>

</xsl:template>


<!--######## handshape2 ########-->
<!--============================-->
<xsl:template match="handshape2">
    <!-- <!ELEMENT handshape2 (handshape1, handshape1?)> -->

    <xsl:choose>

        <xsl:when test="count(*)=1">
            <xsl:apply-templates select="*"/>
        </xsl:when>

        <xsl:when test="count(*)=2">
            <xsl:element name="split_handconfig">
                <xsl:apply-templates select="*"/>
            </xsl:element>
        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## handshape1 ########-->
<!--============================-->
<xsl:template match="handshape1">
    <!--
    <!ELEMENT handshape1 (
        fingernothumb*, fingershape*, fingercrossing*, thumbspecial?
    )>
    <!ATTLIST handshape1
        handshapeclass (%handshapeclass;) #REQUIRED
        fingerbending (%fingerbending;) #IMPLIED
        thumbpos (%thumbpos;) #IMPLIED
        second_handshapeclass (%handshapeclass;) #IMPLIED
        second_fingerbending (%fingerbending;) #IMPLIED
        second_thumbpos (%thumbpos;) #IMPLIED
        approx_shape %boolfalse;
    >
    -->

    <xsl:element name="handconfig">

        <xsl:variable name="hs">
            <xsl:call-template name="handShapeValue"/>
        </xsl:variable>

        <!-- handshape ATTRIBUTE -->
        <xsl:attribute name="handshape">
            <xsl:value-of select="$hs"/>
        </xsl:attribute>

        <xsl:choose>

            <xsl:when test="$hs='fist'">
                <xsl:call-template name="doFistHSAttribs"/>
            </xsl:when>

            <xsl:when test="$hs='flat' or $hs='finger2345'">
                <xsl:call-template name="doFlatHSAttribs"/>
            </xsl:when>

            <xsl:when test="$hs='finger2'">
                <xsl:call-template name="doFinger2HSAttribs"/>
            </xsl:when>

            <xsl:when test="$hs='finger23' or $hs='finger23spread'">
                <xsl:call-template name="doFinger23HSAttribs"/>
            </xsl:when>

            <xsl:when test="starts-with($hs,'pinch')">
                <xsl:call-template name="doPinchHSAttribs">
                    <xsl:with-param name="hs" select="$hs"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="starts-with($hs,'cee')">
                <xsl:call-template name="doCeeHSAttribs">
                    <xsl:with-param name="hs" select="$hs"/>
                </xsl:call-template>
            </xsl:when>

        </xsl:choose>

        <!-- ATTRIBUTES FOR SECOND (BETWEENNESS) HANDSHAPE, IF ANY -->
        <xsl:if test="@second_handshapeclass">

            <xsl:variable name="hsb">
                <xsl:call-template name="secondHandShapeValue"/>
            </xsl:variable>

            <xsl:attribute name="second_handshape">
                <xsl:value-of select="$hsb"/>
            </xsl:attribute>

            <xsl:if test="@second_fingerbending">
                <xsl:if test="$hsb!='fist'">

                    <xsl:attribute name="second_mainbend">
                        <xsl:call-template name='fBendOld2New'>
                            <xsl:with-param name="fb"
                                select="string(@second_fingerbending)"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>

            <xsl:if test="@second_thumbpos">

                <xsl:variable name="tpb">
                    <xsl:call-template name="secondThumbposValue"/>
                </xsl:variable>

                <xsl:choose>

                    <xsl:when test="$hsb='fist' or $hsb='flat' or
                            $hsb='finger2345' or $hsb='finger2' or
                            $hsb='finger23' or $hsb='finger23spread'">
                        <xsl:attribute name="second_thumbpos">
                            <xsl:value-of select="$tpb"/>
                        </xsl:attribute>
                    </xsl:when>

                    <xsl:when test="starts-with($hsb,'cee')">
                         <xsl:if test="$tpb!='out'">
                            <xsl:attribute name="second_ceeopening">
                                <xsl:call-template
                                        name="thumbPos2CeeOpening">
                                    <xsl:with-param
                                            name="thp" select="$tpb"/>
                                </xsl:call-template>
                            </xsl:attribute>
                        </xsl:if>
                    </xsl:when>

                    <!-- test="starts-with($hsb,'pinch')":  DO NOTHING -->
                </xsl:choose>
            </xsl:if>   <!-- test="@second_thumbpos" -->
        </xsl:if>   <!-- test="@second_handshapeclass" -->

        <!-- ADDED IN 2011-04: -->
        <xsl:call-template name="checkApproxHSAttrib"/>

        <!-- ADDED IN 2011-04: -->
        <!-- DEAL WITH THE CASE WHERE THE handshape IS THE OPERAND -->
        <!-- OF A changeposture WITH A replace_incrdecr ATTRIBUTE. -->
        <xsl:if test="parent::*[name()='replacement']/@replace_incrdecr">
          <xsl:variable name="rid">
            <xsl:value-of select="../@replace_incrdecr"/>
          </xsl:variable>
          <xsl:attribute name="successivefingers">
            <xsl:choose>
              <xsl:when test="$rid='ham_increasing'"
                  >towards_pinky</xsl:when>
              <xsl:when test="$rid='ham_decreasing'"
                  >towards_thumb</xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:if>

    </xsl:element>

</xsl:template>


<!--######## fingershape ########-->
<!--=============================-->
<xsl:template match="fingershape">
    <!--
    <!ELEMENT fingershape (finger?)>
    <!ATTLIST fingershape
        fingerbending (%fingerbending;) #REQUIRED
    >
    -->

    <xsl:variable name="dig">
        <xsl:choose>
            <xsl:when test="finger">
                <xsl:apply-templates select="finger"/>
            </xsl:when>
            <xsl:otherwise>2</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:attribute name="{concat('bend',$dig)}">
        <xsl:call-template name='fBendOld2New'>
            <xsl:with-param name="fb" select="string(@fingerbending)"/>
        </xsl:call-template>
    </xsl:attribute>

</xsl:template>


<!--######## fingercrossing ########-->
<!--================================-->
<xsl:template match="fingercrossing">
    <!-- <!ELEMENT fingercrossing (finger, fingerpart, finger)> -->
    <xsl:param name="tag"/>

    <xsl:attribute name="{concat($tag,'contactpair')}">
        <xsl:apply-templates select='finger'/>
    </xsl:attribute>

    <xsl:attribute name="{concat($tag,'contactkind')}">
        <xsl:apply-templates select='fingerpart'/>
    </xsl:attribute>

</xsl:template>


<!--######## thumbspecial ########-->
<!--===============================-->
<xsl:template match="thumbspecial">
    <!--
    <!ELEMENT thumbspecial (
        (fingernothumb, fingernothumb)
      | hambetween
      | thumb
      | fingerpart
    )>
    -->

    <xsl:choose>

        <xsl:when test="fingernothumb">
            <xsl:attribute name="thumbbetween">
                <xsl:apply-templates select="fingernothumb"/>
            </xsl:attribute>
        </xsl:when>

        <xsl:when test="hambetween">
            <xsl:attribute name="thumbenclosed">true</xsl:attribute>
        </xsl:when>

        <!-- thumb IS ALWAYS DONE ELSEWHERE -->
        <xsl:when test="thumb"/>

        <xsl:when test="fingerpart">
            <xsl:attribute name="thumbcontact">
                <xsl:apply-templates select="fingerpart"/>
            </xsl:attribute>
        </xsl:when>

    </xsl:choose>

</xsl:template>

<!--######## finger ########-->
<!--========================-->
<xsl:template match="finger">
    <!--
    <!ELEMENT finger EMPTY>
    <!ATTLIST finger
        fingerid (%finger;) #REQUIRED
    >
    -->

    <!-- JUST RETURN THE SINGLE-DIGIT TEXT FOR THE finger: -->

    <xsl:value-of select="substring-after(@fingerid,'ham_digit_')"/>

</xsl:template>


<!--######## fingernothumb ########-->
<!--===============================-->
<xsl:template match="fingernothumb">
    <!--
    <!ELEMENT fingernothumb EMPTY>
    <!ATTLIST fingernothumb
        fingerid (%fingernothumb;) #REQUIRED
        thumbopp %boolfalse;
    >
    -->

    <!-- JUST RETURN THE SINGLE-DIGIT TEXT FOR THE finger: -->

    <xsl:value-of select="substring-after(@fingerid,'ham_digit_')"/>

</xsl:template>


<!--######## fingerpart ########-->
<!--============================-->
<xsl:template match="fingerpart">
    <!--
    <!ELEMENT fingerpart EMPTY>
    <!ATTLIST fingerpart
        fingerpart (%fingerpart;) #REQUIRED
    >
    -->

    <!-- JUST RETURN THE fingerpart TEXT: -->

    <xsl:value-of select="substring-after(@fingerpart,'ham_finger_')"/>

</xsl:template>


<!--######## extfidir1 ########-->
<!--===========================-->
<xsl:template match="extfidir1">
    <!--
    <!ELEMENT extfidir1 EMPTY>
    <!ATTLIST extfidir1
        extfidir (%the26directions;) #REQUIRED
        second_extfidir (%the26directions;) #IMPLIED
        approx_extfidir %boolfalse;
        abs_extfidir %boolfalse;
        rel_extfidir %boolfalse;
    >
    -->

    <!-- JUST DO THE MAIN extfidir AND rel_extfidir FOR THE PRESENT -->
    <!-- 2002-08-22:  ... AND NOW ALSO second_extfidir              -->

    <xsl:attribute name="extfidir">
        <xsl:value-of select="substring-after(@extfidir,'direction_')"/>
    </xsl:attribute>
    <xsl:if test="@second_extfidir">
        <xsl:attribute name="second_extfidir">
            <xsl:value-of select=
                "substring-after(@second_extfidir,'direction_')"/>
        </xsl:attribute>
    </xsl:if>
    <xsl:call-template name="checkTrueAttrib">
        <xsl:with-param name="aname" select="'rel_extfidir'"/>
    </xsl:call-template>

</xsl:template>


<!--######## palmor1 ########-->
<!--=========================-->
<xsl:template match="palmor1">
    <!--
    <!ELEMENT palmor1 EMPTY>
    <!ATTLIST palmor1
        palmor (%palmor;) #REQUIRED
        second_palmor (%palmor;) #IMPLIED
        approx_palmor %boolfalse;
        abs_palmor %boolfalse;
        rel_palmor %boolfalse;
    >
    -->

    <!-- JUST DO THE MAIN palmor AND rel_palmor FOR THE PRESENT -->
    <!-- 2002-08-22:  ... AND NOW ALSO second_palmor            -->

    <xsl:attribute name="palmor">
        <xsl:value-of select="substring-after(@palmor,'ham_palm_')"/>
    </xsl:attribute>
    <xsl:if test="@second_palmor">
        <xsl:attribute name="second_palmor">
            <xsl:value-of select=
                "substring-after(@second_palmor,'ham_palm_')"/>
        </xsl:attribute>
    </xsl:if>
    <!-- ADDED approx_palmor CHECK IN 2011-04: -->
    <xsl:call-template name="checkTrueAttrib">
        <xsl:with-param name="aname" select="'approx_palmor'"/>
    </xsl:call-template>
    <xsl:call-template name="checkTrueAttrib">
        <xsl:with-param name="aname" select="'rel_palmor'"/>
    </xsl:call-template>

</xsl:template>


<!--===========================-->
<!--######## LOCATIONS ########-->
<!--===========================-->


<!--######## location2 ########-->
<!--===========================-->
<xsl:template match="location2">
    <!--
    <!ELEMENT location2 (
        (location1, location1?)
      | (handconstellation, (locationbody | hamneutral)?)
    )>
    -->

    <xsl:choose>

        <xsl:when test="name(*[1])='location1'">

            <xsl:choose>

                <xsl:when test="count(*)=1">
                    <xsl:apply-templates select="*"/>
                </xsl:when>

                <xsl:when test="count(*)=2">
                    <xsl:element name="split_location">
                        <xsl:apply-templates select="*"/>
                    </xsl:element>
                </xsl:when>

            </xsl:choose>

        </xsl:when>

        <xsl:when test="name(*[1])='handconstellation'">

            <xsl:element name="handconstellation">
                <xsl:apply-templates select="*"/>
            </xsl:element>

        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## handconstellation ########-->
<!--===================================-->
<xsl:template match="handconstellation">
    <!--
    <!ELEMENT handconstellation (
        (locationhand, locationhand)?, contacthand
    )>
    -->

    <!-- IN THIS CONTEXT, THE contacthand OUGHT TO BE SIMPLE: -->
    <!-- ENSURE ATTRIBUTES ARE GENERATED FIRST -->
    <!-- (SEEMS TO BE REQUIRED BY XT - CORRECTLY?) -->
<!--
2010-12
Enhanced to cover the case "contacthand/contactofhand", thereby implicitly
contracticting the claim above that the "contacthand" must be simple.
We treat this case as a shorthand for the case where there is a pair
of explicit "locationhand"s that happen to be identical to one another -
that is, we duplicate the application of the "levelcomplexhand" template,
wrapping each application in a "location_hand" element.
-->

    <xsl:choose>

        <xsl:when test="not(contacthand/contactofhand)">
            <xsl:apply-templates select=
                "contacthand/pcontact | contacthand/ccontact"/>
            <xsl:apply-templates select="locationhand"/>
        </xsl:when>

        <xsl:when test="contacthand/contactofhand">
            <xsl:apply-templates select=
                "contacthand/contactofhand/pcontact"/>
            <xsl:element name="location_hand">
                <xsl:apply-templates select=
                    "contacthand/contactofhand/levelcomplexhand"/>
            </xsl:element>
            <xsl:element name="location_hand">
                <xsl:apply-templates select=
                    "contacthand/contactofhand/levelcomplexhand"/>
            </xsl:element>
        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## location1 ########-->
<!--===========================-->
<xsl:template match="location1">
    <!--
    <!ELEMENT location1 (
        locationbodyarm | locationhand | use_locname
    )>
    -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## use_locname ########-->
<!--=============================-->
<xsl:template match="use_locname">
    <!--
    <!ELEMENT use_locname EMPTY>
    <!ATTLIST use_locname
        use_locname %locname; #REQUIRED
    >
    -->

    <xsl:apply-templates select="*"/>
    <xsl:element name="use_locname">
        <xsl:attribute name="use_locname">
            <xsl:value-of select="substring-after(@use_locname,'loc')"/>
        </xsl:attribute>
    </xsl:element>

</xsl:template>


<!--######## locationbodyarm ########-->
<!--=================================-->
<xsl:template match="locationbodyarm">
    <!--
    <!ELEMENT locationbodyarm (
        locationbody | locationarm | hamneutral
    )>
    -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## locationbody ########-->
<!--==============================-->
<xsl:template match="locationbody">
    <!--
    <!ELEMENT locationbody (
        levelcomplexbody, hambehind?, contactbody?
    )>
    -->

    <xsl:element name="location_bodyarm">
        <xsl:apply-templates select="levelcomplexbody"/>
        <xsl:if test="hambehind">
           <xsl:attribute name="behind">true</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="contactbody"/>
    </xsl:element>

</xsl:template>


<!--######## locationarm ########-->
<!--=============================-->
<xsl:template match="locationarm">
    <!--
    <!ELEMENT locationarm (
        levelcomplexarm, hambehind?, contactbody?
    )>
    -->

    <xsl:element name="location_bodyarm">
        <xsl:apply-templates select="levelcomplexarm"/>
        <xsl:if test="hambehind">
           <xsl:attribute name="behind">true</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="contactbody"/>
    </xsl:element>

</xsl:template>


<!--######## locationhand ########-->
<!--==============================-->
<xsl:template match="locationhand">
    <!-- <!ELEMENT locationhand (levelcomplexhand, contacthand?)> -->

    <xsl:element name="location_hand">
        <xsl:apply-templates select="levelcomplexhand"/>
        <xsl:apply-templates select="contacthand"/>
    </xsl:element>

</xsl:template>


<!--######## levelcomplexbody ########-->
<!--######## levelcomplexarm ########-->
<!--######## levelcomplexhand ########-->
<!--==================================-->
<xsl:template match="levelcomplexbody | levelcomplexarm | levelcomplexhand">
    <!-- <!ELEMENT levelcomplexbody (levelbody, levelbody?)> -->
    <!-- <!ELEMENT levelcomplexarm (levelarm, levelarm?)> -->
    <!-- <!ELEMENT levelcomplexhand (levelhand, levelhand?)> -->

    <xsl:apply-templates select="*[1]">
        <xsl:with-param name="tag" select="''"/>
    </xsl:apply-templates> 

    <xsl:if test="count(*)=2">
        <xsl:apply-templates select="*[2]">
            <xsl:with-param name="tag" select="'second_'"/>
        </xsl:apply-templates> 
    </xsl:if>

</xsl:template>


<!--######## levelbody ########-->
<!--===========================-->
<xsl:template match="levelbody">
    <!--
    <!ELEMENT levelbody EMPTY>
    <!ATTLIST levelbody
        locbody (%locbody;) #REQUIRED
        %loc_attribs;
    >
    -->
    <xsl:param name="tag"/>

    <xsl:variable name="site">
        <xsl:value-of select="substring-after(@locbody,'ham_')"/>
    </xsl:variable>

    <xsl:attribute name="{concat($tag,'location')}">
        <xsl:value-of select="$site"/>
    </xsl:attribute>

    <xsl:if test="@side">
        <xsl:attribute name="{concat($tag,'side')}">
            <xsl:value-of select="substring-after(@side,'ham_')"/>
        </xsl:attribute>
    </xsl:if>
    <!-- T.B.D.: NON-HAMNOSYS-4 ATTRIBUTES -->

    <!-- ADDED IN 2011-04: -->
    <xsl:call-template name="checkTrueAttrib">
        <xsl:with-param name="aname" select="'approx_location'"/>
    </xsl:call-template>

</xsl:template>


<!--######## levelbody (for non-manual contexts) ########-->
<!--===========================-->
<xsl:template match="levelbody" mode="inNonManContext">

    <xsl:element name="handconfig">
        <xsl:attribute name="bodypart">
            <xsl:value-of select="substring-after(@locbody,'ham_')"/>
        </xsl:attribute>
        <xsl:if test="@side">
            <xsl:attribute name="side">
                <xsl:value-of select="substring-after(@side,'ham_')"/>
            </xsl:attribute>
        </xsl:if>
        <!-- T.B.D.: ? NON-HAMNOSYS-4 ATTRIBUTES -->
    </xsl:element>

</xsl:template>


<!--######## levelarm ########-->
<!--==========================-->
<xsl:template match="levelarm">
    <!--
    <!ELEMENT levelarm EMPTY>
    <!ATTLIST levelarm
        locarm (%locarm;) #REQUIRED
        %loc_attribs;
        dorsal_or_palmar (dorsal | palmar) #IMPLIED
    >
    -->
    <xsl:param name="tag"/>

    <xsl:attribute name="{concat($tag,'location')}">
        <xsl:value-of select="substring-after(@locarm,'ham_')"/>
    </xsl:attribute>

    <xsl:if test="@side">
        <xsl:attribute name="{concat($tag,'side')}">
            <xsl:value-of select="substring-after(@side,'ham_')"/>
        </xsl:attribute>
    </xsl:if>
    <!-- T.B.D.: NON-HAMNOSYS-4 ATTRIBUTES ... -->

    <xsl:if test="@dorsal_or_palmar">
        <xsl:attribute name="{concat($tag, 'side')}">
            <xsl:value-of select="@dorsal_or_palmar"/>
        </xsl:attribute>
    </xsl:if>

    <!-- ADDED IN 2011-04: -->
    <xsl:call-template name="checkTrueAttrib">
        <xsl:with-param name="aname" select="'approx_location'"/>
    </xsl:call-template>

</xsl:template>


<!--######## levelarm (for non-manual motion) ########-->
<!--==================================================-->
<xsl:template match="levelarm" mode="inNonManContext">

    <xsl:element name="handconfig">
        <xsl:attribute name="bodypart">
            <xsl:value-of select="substring-after(@locarm,'ham_')"/>
        </xsl:attribute>
        <xsl:if test="@side">
            <xsl:attribute name="side">
                <xsl:value-of select="substring-after(@side,'ham_')"/>
            </xsl:attribute>
        </xsl:if>
        <!-- T.B.D.: ? NON-HAMNOSYS-4 ATTRIBUTES ... -->
        <xsl:if test="@dorsal_or_palmar">
            <xsl:attribute name="side">
                <xsl:value-of select="@dorsal_or_palmar"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:element>
</xsl:template>


<!--######## levelhand ########-->
<!--===========================-->
<xsl:template match="levelhand">
    <!--
    <!ELEMENT levelhand (
        handpart
      | (fingerpart, finger*)
      | ( finger, ( finger* | fingerpart+ ) )
    )>
    <!ATTLIST levelhand
        %loc_attribs;
        dorsal_or_palmar (dorsal | palmar) #IMPLIED
    >
    -->
    <xsl:param name="tag"/>

    <xsl:apply-templates select="handpart">
        <xsl:with-param name="tag" select="$tag"/>
    </xsl:apply-templates>

    <!-- T.B.D.: MORE WORK ON INTER-RELATEDNESS OF fingerpart AND side -->

    <xsl:if test="fingerpart">

        <xsl:attribute name="{concat($tag,'location')}">
            <xsl:apply-templates select="fingerpart"/>
<!--
####  2003-08  Experimental support for multiple finger parts:
            <xsl:apply-templates select="fingerpart"/>
            <xsl:for-each select="fingerpart[position()!=1]">
              <xsl:text> </xsl:text><xsl:apply-templates select="."/>
            </xsl:for-each>
-->
        </xsl:attribute>
    </xsl:if>

    <!-- T.B.D.: COVER (finger, fingerpart+) CASE -->

    <xsl:if test="finger">
        <xsl:attribute name="{concat($tag,'digits')}">
            <xsl:apply-templates select="finger"/>
        </xsl:attribute>
    </xsl:if>

    <!-- T.B.D.: SOME INTEGRITY CHECKS FOR side ATTRIBUTE ... -->

    <xsl:if test="@side">
        <xsl:attribute name="{concat($tag,'side')}">
            <xsl:value-of select="substring-after(@side,'ham_')"/>
        </xsl:attribute>
    </xsl:if>

    <xsl:if test="@dorsal_or_palmar">
        <xsl:attribute name="{concat($tag, 'side')}">
            <xsl:value-of select="@dorsal_or_palmar"/>
        </xsl:attribute>
    </xsl:if>

</xsl:template>


<!--######## contactbody ########-->
<!--=============================-->
<xsl:template match="contactbody">
    <!--
    <!ELEMENT contactbody (
        pcontact | hamarmextended | contactofhand
    )>
    -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## contacthand ########-->
<!--=============================-->
<xsl:template match="contacthand">
    <!--
    <!ELEMENT contacthand (
        pcontact | ccontact | contactofhand
    )>
    -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## contactofhand ########-->
<!--===============================-->
<xsl:template match="contactofhand">
    <!--
    <!ELEMENT contactofhand (
        (pcontact | ccontact),
        (levelcomplexhand | levelcomplexarm)
    )>
    -->
<!--
2003-08-26 Now we have to cater for the levelcomplexarm alternative.
-->

    <xsl:apply-templates select="pcontact | ccontact"/>

    <xsl:element name="location_hand">
        <xsl:apply-templates select="levelcomplexhand | levelcomplexarm"/>
    </xsl:element>

</xsl:template>


<!--######## handpart ########-->
<!--==========================-->
<xsl:template match="handpart">
    <!--
    <!ELEMENT handpart EMPTY>
    <!ATTLIST handpart
        handpart (%handpart;) #REQUIRED
    >
    -->
    <xsl:param name="tag"/>

    <xsl:attribute name="{concat($tag,'location')}">
        <xsl:value-of select="substring-after(@handpart,'ham_')"/>
    </xsl:attribute>

</xsl:template>


<!--######## pcontact ########-->
<!--==========================-->
<xsl:template match="pcontact">
    <!--
    <!ELEMENT pcontact EMPTY>
    <!ATTLIST pcontact
        where (%pcontact;) #REQUIRED
    >
    -->

    <xsl:attribute name="contact">
        <xsl:value-of select="substring-after(@where,'ham_')"/>
    </xsl:attribute>

</xsl:template>


<!--######## ccontact ########-->
<!--==========================-->
<xsl:template match="ccontact">
    <!--
    <!ELEMENT ccontact EMPTY>
    <!ATTLIST ccontact
        crosskind (%ccontact;) #REQUIRED
    >
    -->

    <xsl:attribute name="contact">
        <xsl:value-of select="substring-after(@crosskind,'ham_')"/>
    </xsl:attribute>

</xsl:template>


<!--######## hamarmextended ########-->
<!--==========================-->
<xsl:template match="hamarmextended">
    <!-- <!ELEMENT hamarmextended EMPTY> -->

    <xsl:attribute name="contact">armextended</xsl:attribute>

</xsl:template>


<!--######## hamneutral ########-->
<!--============================-->
<xsl:template match="hamneutral">
    <!--
    <!ELEMENT hamneutral EMPTY>
    <!ATTLIST hamneutral
        armextended %boolfalse;
    >
    -->

    <xsl:element name="location_bodyarm">
        <xsl:if test="@armextended='true'">
            <xsl:attribute name="contact">armextended</xsl:attribute>
        </xsl:if>
    </xsl:element>

</xsl:template>


<!--====================================-->
<!--######## STRUCTURED ACTIONS ########-->
<!--====================================-->


<!--######## action2t, action1t ########-->
<!--====================================-->
<xsl:template match="action2t | action1t">

    <!-- DEAL WITH repetitionS HERE AND DELEGATE -->
    <!-- THE REST TO THE "PROPER" RULES BELOW.  -->

    <xsl:choose>

        <xsl:when test="not(@repetition)">
            <xsl:apply-templates select="." mode="ignoreRepetitions"/>
        </xsl:when>

        <xsl:when test="@repetition">
            <xsl:element name="rpt_motion">
                <xsl:call-template name="setMainRepetitionAttribs"/>
                <xsl:call-template name="checkCurrentAlternating"/>
                <xsl:apply-templates select="." mode="ignoreRepetitions"/>
            </xsl:element>
        </xsl:when>
        
    </xsl:choose>

</xsl:template>


<!--######## action2t (ignoring repetitions) ########-->
<!--=================================================-->
<xsl:template match="action2t" mode="ignoreRepetitions">
    <!--
    <!ELEMENT action2t (
        (action1t, action1t?)
      | (action2, location2)
      | (action1, location2)
      | action2t
      | par_action2t
      | seq_action2t
    )>
    <!ATTLIST action2t
        repetition ( %repetition; ) #IMPLIED
        second_repetition ( %repetition; ) #IMPLIED
        repetition_incrdecr ( %incrdecr; ) #IMPLIED
        repetition_baseshift ( %movementarrow; ) #IMPLIED
        baseshift_incrdecr ( %incrdecr; ) #IMPLIED
        alternating  %boolfalse;
        second_alternating  %boolfalse;
    >
    -->

    <!-- TBD: allow (action2t, location2) and wrap as tgt_motion -->

    <xsl:choose>

        <xsl:when test="name(*[1])='action1t'">

            <xsl:choose>

                <xsl:when test="count(*)!=2">
                    <xsl:apply-templates select="*"/>
                </xsl:when>

                <!--
                2011-01-08
                I think we should use checkAncestorAlternating here,
                rather than checkCurrentAlternating: putting alternating
                attributes here makes sense only when the parent of the
                new <split_motion> is not a <rpt_motion> - to which later any
                necessary alternating attributes will already have been
                attached.
				-->
                <xsl:when test="count(*)=2">
                    <xsl:element name="split_motion">
                        <xsl:call-template name=
                            "checkAncestorAlternating"/>
                        <xsl:apply-templates select="*"/>
                    </xsl:element>
                </xsl:when>

            </xsl:choose>

        </xsl:when>

        <xsl:when test="name(*[1])='action1' or name(*[1])='action2'">

            <xsl:element name="tgt_motion">
                <xsl:call-template name="checkCurrentAlternating"/>
                <xsl:apply-templates select="*"/>
            </xsl:element>

        </xsl:when>

        <xsl:when test="action2t | par_action2t | seq_action2t">

            <xsl:apply-templates select="*"/>

        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## SUPPORT FOR action1t WITH A TARGET ########-->
<!--====================================================-->
<xsl:template name="targetedAction1t">
    <!--
    <!ELEMENT action1t (
        ...
      | (action1, (location1 | handconstellation)?)
      | ...
    )>
    -->

    <xsl:element name="tgt_motion">

        <xsl:call-template name="checkCurrentAlternating"/>

        <!-- SPECIAL TREATMENT FOR A replacement ACTION1: -->

        <xsl:choose>

            <xsl:when test="action1/simplemovement/replacement">
                <xsl:apply-templates
                    select="action1/simplemovement/replacement"
                    mode="noTargetWrapper"/>
            </xsl:when>

            <xsl:otherwise>
                <xsl:apply-templates select="action1"/>
            </xsl:otherwise>

        </xsl:choose>

        <xsl:apply-templates select="location1"/>

        <!-- handconstellation SHOULD BE WRAPPED BY ITS PARENT: -->
        <xsl:if test="handconstellation">
            <xsl:element name="handconstellation">
                <xsl:apply-templates
                    select="handconstellation"/>
            </xsl:element>
        </xsl:if>

    </xsl:element>

</xsl:template>


<!--######## action1t (ignoring repetitions) ########-->
<!--=================================================-->
<xsl:template match="action1t" mode="ignoreRepetitions">
    <!--
    <!ELEMENT action1t (
        hamnomotion
      | (action1, (location1 | handconstellation)?)
      | ((levelbody | levelarm), action1)
      | action1t
      | par_action1t
      | seq_action1t
    )>
    <!ATTLIST action1t
        repetition ( %repetition; ) #IMPLIED
        second_repetition ( %repetition; ) #IMPLIED
        repetition_incrdecr ( %incrdecr; ) #IMPLIED
        repetition_baseshift ( %movementarrow; ) #IMPLIED
        baseshift_incrdecr ( %incrdecr; ) #IMPLIED
        alternating  %boolfalse;
        second_alternating  %boolfalse;
    >
    -->

    <!-- TBD: allow (action1t, location1) and wrap as tgt_motion -->

    <!-- IGNORE NON-MANUAL ACTIONS FOR NOW -->

    <xsl:choose>

        <xsl:when test="name(*[1])='action1'">

            <xsl:choose>

                <xsl:when test="count(*)!=2">
                    <xsl:apply-templates select="*"/>
                </xsl:when>

                <xsl:when test="count(*)=2">
                    <xsl:call-template name="targetedAction1t"/>
                </xsl:when>

            </xsl:choose>

        </xsl:when>

        <xsl:when test="name(*[1])='levelbody' or name(*[1])='levelarm'">
            <xsl:element name="nonman_motion">
                <xsl:apply-templates select="*[1]" mode="inNonManContext"/>
								<!-- #### 2007-03 #### -->
                <xsl:apply-templates select ="action1t"/>
            </xsl:element>
        </xsl:when>

        <xsl:when test=
            "hamnomotion | action1t | par_action1t | seq_action1t">

            <xsl:apply-templates select="*"/>

        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## par_action2t ########-->
<!--==============================-->
<xsl:template match="par_action2t">
    <!-- <!ELEMENT par_action2t ( action2t, action2t+ )> -->

    <xsl:element name="par_motion">
        <xsl:call-template name="checkAncestorAlternating"/>
        <xsl:apply-templates select="*"/>
    </xsl:element>

</xsl:template>


<!--######## seq_action2t ########-->
<!--==============================-->
<xsl:template match="seq_action2t">
    <!--
    <!ELEMENT seq_action2t ( action2t, action2t+ )>
    <!ATTLIST seq_action2t
        fused  %boolfalse;
    >
    -->

    <xsl:element name="seq_motion">

        <xsl:call-template name="checkTrueAttrib">
            <xsl:with-param name="aname" select="'fused'"/>
        </xsl:call-template>

        <xsl:call-template name="checkAncestorAlternating"/>

        <xsl:apply-templates select="*"/>

    </xsl:element>

</xsl:template>


<!--######## action2 ########-->
<!--=========================-->
<xsl:template match="action2">
    <!-- <!ELEMENT action2 (action1, action1?)> -->

    <xsl:choose>

        <xsl:when test="count(*)=1">
            <xsl:apply-templates select="*"/>
        </xsl:when>

        <xsl:when test="count(*)!=1">
            <xsl:element name="split_motion">
                <xsl:call-template name="checkAncestorAlternating"/>
                <xsl:apply-templates select="*"/>
            </xsl:element>
        </xsl:when>

    </xsl:choose>

</xsl:template>


<!--######## par_action1t ########-->
<!--==============================-->
<xsl:template match="par_action1t">
    <!-- <!ELEMENT par_action1t ( actiont, action1t+ )> -->

    <xsl:element name="par_motion">
        <xsl:call-template name="checkAncestorAlternating"/>
        <xsl:apply-templates select="*"/>
    </xsl:element>

</xsl:template>


<!--######## seq_action1t ########-->
<!--==============================-->
<xsl:template match="seq_action1t">
    <!--
    <!ELEMENT seq_action1t ( actiont, action1t+ )>
    <!ATTLIST seq_action1t
        fused  %boolfalse;
    >
    -->

    <xsl:element name="seq_motion">

        <xsl:call-template name="checkTrueAttrib">
            <xsl:with-param name="aname" select="'fused'"/>
        </xsl:call-template>

        <xsl:call-template name="checkAncestorAlternating"/>

        <xsl:apply-templates select="*"/>

    </xsl:element>

</xsl:template>


<!--######## action1 ########-->
<!--=========================-->
<xsl:template match="action1">
    <!--
    <!ELEMENT action1 (
        hamnomotion | simplemovement | par_action1 | seq_action1
    )>
    <!ATTLIST action1
        bouncing %boolfalse;
    >
    -->

    <!-- LET DESCENDANT MOTION DEAL WITH bouncing ATTRIBUTE. -->

    <xsl:apply-templates select="*"/>

</xsl:template>


<!--######## par_action1 ########-->
<!--=============================-->
<xsl:template match="par_action1">
    <!-- <!ELEMENT par_action1 ( action1, action1+ )> -->

    <xsl:element name="par_motion">
        <xsl:call-template name="checkAncestorAlternating"/>
        <xsl:apply-templates select="*"/>
    </xsl:element>

</xsl:template>


<!--######## seq_action1 ########-->
<!--=============================-->
<xsl:template match="seq_action1">
    <!--
    <!ELEMENT seq_action1 ( action1, action1+ )>
    <!ATTLIST seq_action1
        fused  %boolfalse;
    >
    -->

    <xsl:element name="seq_motion">

        <xsl:call-template name="checkTrueAttrib">
            <xsl:with-param name="aname" select="'fused'"/>
        </xsl:call-template>

        <xsl:call-template name="checkAncestorAlternating"/>

        <xsl:apply-templates select="*"/>

    </xsl:element>

</xsl:template>


<!--================================-->
<!--######## SIMPLE ACTIONS ########-->
<!--================================-->


<!--######## simplemovement ########-->
<!--================================-->
<xsl:template match="simplemovement">
    <!--
    <!ELEMENT simplemovement (
        (
            straightmovement | circularmovement | wristmovement
            | movementcross | replacement | hamfingerplay
        ),
        ( location1 )?
    )>
    <!ATTLIST simplemovement
        modifier (%modifier;) #IMPLIED
        def_locname (%locname;) #IMPLIED
        abs_motion %boolfalse;
    >
    -->

    <!-- LEAVE THE CHILD TO SORT OUT OUR ATTRIBUTES    -->
    <!-- (AND ANY BRUSHING CONTACT location ELEMENT).  -->
    <!-- NB: A replacement PROCESSED VIA THIS TEMPLATE -->
    <!-- WILL BE GIVEN AN ENCLOSING tgt_motion.        -->

    <!-- NB: movementcross IS IGNORED HERE AT PRESENT. -->

    <!-- DIRECTLY PROCESS THE MOVEMENT CHILD ONLY:  -->
    <xsl:apply-templates select="*[1]"/>

</xsl:template>


<!--######## straightmovement ########-->
<!--==================================-->
<xsl:template match="straightmovement">
    <!--
    <!ELEMENT straightmovement EMPTY>
    <!ATTLIST straightmovement
        movement (%movementarrow;) #REQUIRED
        size (%size;) #IMPLIED
        second_movement (%movementarrow;) #IMPLIED
        second_size (%size;) #IMPLIED

        arc (%arc;) #IMPLIED
        arc_size (%size;) #IMPLIED

        zigzagstyle (ham_zigzag | ham_wavy) #IMPLIED
        zigzag_size (%size;) #IMPLIED
        ellipsedirection (%ellipsedirection;) #IMPLIED
        %dynamicsize_attribs;
    >
    -->

    <xsl:element name="directedmotion">

        <!-- 2003-08-19:
        this is WRONG: meaning of second_movement is betweenness

        <xsl:attribute name="direction">
            <xsl:call-template name="pairOf18sTo26">
                <xsl:with-param name="stra"
                    select="substring-after(@movement,'ham_move_')"/>
                <xsl:with-param name="strb"
                    select="substring-after(@second_movement,'ham_move_')"/>
            </xsl:call-template>
        </xsl:attribute>
        -->

        <xsl:attribute name="direction">
            <xsl:value-of select=
                "substring-after(@movement,'ham_move_')"/>
        </xsl:attribute>
        <xsl:if test="@second_movement">
            <xsl:attribute name="second_direction">
                <xsl:value-of select=
                    "substring-after(@second_movement,'ham_move_')"/>
            </xsl:attribute>
        </xsl:if>

        <!-- size ATTRIBUTE: -->
        <xsl:call-template name="copySizeAttrib"/>

        <xsl:if test="@arc">
          <xsl:attribute name="curve">
              <xsl:value-of select="substring-after(@arc,'ham_arc_')"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@arc_size">
          <xsl:attribute name="curve_size">
              <xsl:call-template name="convertSize">
                <xsl:with-param name="sz" select="@arc_size"/>
              </xsl:call-template>
          </xsl:attribute>
        </xsl:if>

        <!-- PARENT simplemovement ATTRIBUTES: -->
        <xsl:call-template name="copySimpleParentAttribs"/>

        <!-- zigzag ATTRIBUTES: -->
        <xsl:call-template name="copyZigzagAttribs"/>

        <!-- ellipsedirection ATTRIBUTE: -->
        <xsl:call-template name="copyEllipsedirectionAttrib"/>

        <!-- ENTITY % dynamicsize_attribs: -->
        <xsl:call-template name="copyDynamicsizeAttribs"/>

        <!-- bouncing ATTRIBUTE IN action1 ANCESTOR: -->
        <xsl:call-template name="checkBouncing"/>

        <!-- alternating ATTRIBUTES IN action?t ANCESTOR: -->
        <xsl:call-template name="checkAncestorAlternating"/>

        <!-- DEAL WITH ANY BRUSHING CONTACT SUB-ELEMENT HERE: -->
        <xsl:apply-templates select="following-sibling::location1"/>

    </xsl:element>

</xsl:template>


<!--######## circularmovement ########-->
<!--==================================-->
<xsl:template match="circularmovement">
    <!--
    <!ELEMENT circularmovement (ellipse?)>
    <!ATTLIST circularmovement
        movement (%movementcircle;) #REQUIRED
        size (%size;) #IMPLIED
        second_movement (%movementcircle;) #IMPLIED
        second_size (%size;) #IMPLIED
        redundant_size (%size;) #IMPLIED

        start (%clock;) #IMPLIED
        clockfull %boolfalse;
        second_clockfull %boolfalse;
        end (%clock;) #IMPLIED

        zigzagstyle (ham_zigzag | ham_wavy) #IMPLIED

        %dynamicsize_attribs;
    >
    -->

    <xsl:element name="circularmotion">

        <!-- 2003-08-19:
        this is WRONG: meaning of second_movement is betweenness

        <xsl:attribute name="axis">
            <xsl:call-template name="pairOf18sTo26">
                <xsl:with-param name="stra"
                    select="substring-after(@movement,'ham_circle_')"/>
                <xsl:with-param name="strb"
                    select="substring-after(@second_movement,'ham_circle_')"/>
            </xsl:call-template>
        </xsl:attribute>
        -->

        <xsl:attribute name="axis">
            <xsl:value-of select=
                "substring-after(@movement,'ham_circle_')"/>
        </xsl:attribute>
        <xsl:if test="@second_movement">
            <xsl:attribute name="second_axis">
                <xsl:value-of select=
                    "substring-after(@second_movement,'ham_circle_')"/>
            </xsl:attribute>
        </xsl:if>

        <!-- size ATTRIBUTE: -->
        <xsl:call-template name="copySizeAttrib"/>

        <!-- PARENT simplemovement ATTRIBUTES: -->
        <xsl:call-template name="copySimpleParentAttribs"/>

        <!-- ellipse ELEMENT ATTRIBUTES: -->
        <xsl:apply-templates select="ellipse"/>

        <!-- clock ATTRIBUTES: -->
        <xsl:call-template name="copyClockAttribs"/>

        <!-- zigzag ATTRIBUTES: -->
        <xsl:call-template name="copyZigzagAttribs"/>

        <!-- ENTITY % dynamicsize_attribs: -->
        <xsl:call-template name="copyDynamicsizeAttribs"/>

        <!-- bouncing ATTRIBUTE IN action1 ANCESTOR: -->
        <xsl:call-template name="checkBouncing"/>

        <!-- alternating ATTRIBUTES IN action?t ANCESTOR: -->
        <xsl:call-template name="checkAncestorAlternating"/>

        <!-- DEAL WITH ANY BRUSHING CONTACT SUB-ELEMENT HERE: -->
        <xsl:apply-templates select="following-sibling::location1"/>

    </xsl:element>

</xsl:template>


<!--######## wristmovement ########-->
<!--===============================-->
<xsl:template match="wristmovement">
    <!--
    <!ELEMENT wristmovement EMPTY>
    <!ATTLIST wristmovement
        movement ( %wristmovement; ) #REQUIRED
        size (%size;) #IMPLIED
    >
    -->

    <xsl:element name="wristmotion">

        <xsl:attribute name="motion">
            <xsl:value-of
                select="substring-after(@movement,'ham_wrist_')"/>
        </xsl:attribute>

        <!-- size ATTRIBUTE: -->
        <xsl:call-template name="copySizeAttrib"/>

        <!-- alternating ATTRIBUTES IN action?t ANCESTOR: -->
        <xsl:call-template name="checkAncestorAlternating"/>

    </xsl:element>

</xsl:template>


<!--######## replacement (upper-level) ########-->
<!--===========================================-->
<xsl:template match="replacement">

    <!-- DEFAULT ROUTE TO replacement PROCESSING:   -->
    <!-- AN ENCLOSING tgt_motion ELEMENT IS NEEDED. -->

    <xsl:element name="tgt_motion">
        <xsl:call-template name="checkAncestorAlternating"/>
        <xsl:apply-templates select="." mode="noTargetWrapper"/>
    </xsl:element>

</xsl:template>


<!--######## replacement (lower-level) ########-->
<!--===========================================-->
<xsl:template match="replacement" mode="noTargetWrapper">
    <!--
    <!ELEMENT replacement (
        handshape1?, ((extfidir1?, palmor1?) | splitreplacetail?)
    )>
    <!ATTLIST replacement
        %dynamicsize_attribs;
    >
    -->

    <!-- NB: THIS MAY BE CALLED BOTH BY THE UPPER-LEVEL     -->
    <!-- replacement TEMPLATE ABOVE, AND ALSO BY AN         -->
    <!-- action1t ANCESTOR WHEN IT IS SETTING UP A TARGET   -->
    <!-- MOTION ON ITS OWN ACCOUNT.                         -->

	<!-- PARENT simplemovement ATTRIBUTES: -->
    <xsl:call-template name="copySimpleParentAttribs"/>

    <!-- ENTITY % dynamicsize_attribs: -->
    <xsl:call-template name="copyDynamicsizeAttribs"/>

    <!-- alternating ATTRIBUTES IN action?t ANCESTOR: -->
    <xsl:call-template name="checkAncestorAlternating"/>

    <xsl:element name="changeposture"/>

    <!-- 2011-03: replace MAY HAVE incrdecr ATTRIBUTE:    -->
    <!-- 2011-04: IF SO, WE NOW HANDLE IT IN handshape1.  -->
    <xsl:apply-templates select="handshape1"/>

    <xsl:if test="extfidir1 | palmor1">
        <xsl:element name="handconfig">
            <xsl:apply-templates select="extfidir1 | palmor1"/>
            <xsl:text/>
        </xsl:element>
    </xsl:if>

    <xsl:apply-templates select="splitreplacetail"/>

</xsl:template>


<!--######## splitreplacetail ########-->
<!--==================================-->
<xsl:template match="splitreplacetail">
    <!-- <!ELEMENT splitreplacetail (replacetail1, replacetail1?)> -->

    <xsl:element name="split_handconfig">
        <xsl:apply-templates select="*[1]"/>
        <xsl:choose>
            <xsl:when test="count(*)=2">
                <xsl:apply-templates select="*[2]"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- NULL handconfig for "no-motion" case: -->
                <xsl:element name="handconfig"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:element>

<!--  REPLACED AS ABOVE, 2003-08-19:
    <xsl:if test="count(*)=2">
        <xsl:element name="split_handconfig">
            <xsl:apply-templates select="*"/>
        </xsl:element>
    </xsl:if>
    <xsl:if test="count(*)=1">
        <xsl:apply-templates select="*"/>
    </xsl:if>
-->
</xsl:template>


<!--######## replacetail1 ########-->
<!--==============================-->
<xsl:template match="replacetail1">
    <!-- <!ELEMENT replacetail1 (extfidir1?, palmor1?)> -->

    <xsl:element name="handconfig">
        <xsl:apply-templates select="*"/>
        <xsl:text/>
    </xsl:element>

</xsl:template>


<!--######## hamfingerplay ########-->
<!--===============================-->
<xsl:template match="hamfingerplay">
    <!-- <!ELEMENT hamfingerplay (finger*)> -->

    <xsl:element name="fingerplay">

         <!-- 2011-04: ALLOW finger CHILDREN. -->
        <xsl:if test="finger">
            <xsl:attribute name="digits">
                <xsl:apply-templates select="finger"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:element>

</xsl:template>


<!--######## hamnomotion ########-->
<!--=============================-->
<xsl:template match="hamnomotion">
    <!-- <!ELEMENT hamnomotion EMPTY> -->

    <xsl:element name="nomotion"/>

</xsl:template>

<!--######## ellipse ########-->
<!--=========================-->
<xsl:template match="ellipse">
    <!--
    <!ELEMENT ellipse EMPTY>
    <!ATTLIST ellipse
        ellipsedirection (%ellipsedirection;) #REQUIRED
        size (%size;) #IMPLIED
    >
    -->

    <!-- ellipsedirection ATTRIBUTE: -->
    <xsl:call-template name="copyEllipsedirectionAttrib"/>

    <!-- size ATTRIBUTE: -->
    <xsl:call-template name="copySizeAttrib"/>

</xsl:template>


<!--=================================-->
<!--######## COMMENT COPYING ########-->
<!--=================================-->


<xsl:template match="comment()">

    <xsl:copy/>

</xsl:template>

<!--============= End h2SignsH4.xsl ============-->

<!--======================================-->
<!--######## MATCH A sign ELEMENT ########-->
<!--======================================-->


<xsl:template match="/">

    <xsl:apply-templates select="sign"/>

</xsl:template>


</xsl:transform>
