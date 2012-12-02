<?xml ?>

<helpset>

<title>RosettaNet</title>

<maps>
   <mapref location="as2_en.jhm"/>
</maps>

<links>
</links>

<view>
   <name>TOC</name>
   <label>Content</label>
   <type>oracle.help.navigator.tocNavigator.TOCNavigator</type>
   <data engine="oracle.help.engine.XMLTOCEngine">as2_enTOC.xml</data>
</view>

<view>
   <name>search</name>
   <title>Search</title>
   <type>oracle.help.navigator.searchNavigator.SearchNavigator</type>
   <data engine="oracle.help.engine.SearchEngine">as2_en.idx</data>
</view>

<view>
   <label>Index</label>
   <type>oracle.help.navigator.keywordNavigator.KeywordNavigator</type>
   <data engine="oracle.help.engine.XMLTOCEngine">as2_enTOC.xml</data>
</view>

</helpset>
