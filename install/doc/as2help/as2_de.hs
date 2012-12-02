<?xml ?>

<helpset>

<title>AS2</title>

<maps>
   <mapref location="as2_de.jhm"/>
</maps>

<links>
</links>

<view>
   <name>TOC</name>
   <label>Inhalt</label>
   <type>oracle.help.navigator.tocNavigator.TOCNavigator</type>
   <data engine="oracle.help.engine.XMLTOCEngine">as2_deTOC.xml</data>
</view>

<view>
   <name>search</name>
   <title>Suchen</title>
   <type>oracle.help.navigator.searchNavigator.SearchNavigator</type>
   <data engine="oracle.help.engine.SearchEngine">as2_de.idx</data>
</view>

<view>
   <label>Index</label>
   <type>oracle.help.navigator.keywordNavigator.KeywordNavigator</type>
   <data engine="oracle.help.engine.XMLTOCEngine">as2_deTOC.xml</data>
</view>

</helpset>
