grammar Hive;

options
{
output=AST;
ASTLabelType=CommonTree;
backtrack=true;
k=1;
}
 
tokens {
TOK_INSERT;
TOK_QUERY;
TOK_SELECT;
TOK_SELECTDI;
TOK_SELEXPR;
TOK_FROM;
TOK_TAB;
TOK_PARTSPEC;
TOK_PARTVAL;
TOK_DIR;
TOK_LOCAL_DIR;
TOK_TABREF;
TOK_SUBQUERY;
TOK_DESTINATION;
TOK_ALLCOLREF;
TOK_COLREF;
TOK_FUNCTION;
TOK_FUNCTIONDI;
TOK_WHERE;
TOK_OP_EQ;
TOK_OP_NE;
TOK_OP_LE;
TOK_OP_LT;
TOK_OP_GE;
TOK_OP_GT;
TOK_OP_DIV;
TOK_OP_ADD;
TOK_OP_SUB;
TOK_OP_MUL;
TOK_OP_MOD;
TOK_OP_BITAND;
TOK_OP_BITNOT;
TOK_OP_BITOR;
TOK_OP_BITXOR;
TOK_OP_AND;
TOK_OP_OR;
TOK_OP_NOT;
TOK_OP_LIKE;
TOK_TRUE;
TOK_FALSE;
TOK_TRANSFORM;
TOK_EXPLIST;
TOK_ALIASLIST;
TOK_GROUPBY;
TOK_ORDERBY;
TOK_CLUSTERBY;
TOK_DISTRIBUTEBY;
TOK_SORTBY;
TOK_UNION;
TOK_JOIN;
TOK_LEFTOUTERJOIN;
TOK_RIGHTOUTERJOIN;
TOK_FULLOUTERJOIN;
TOK_LOAD;
TOK_NULL;
TOK_ISNULL;
TOK_ISNOTNULL;
TOK_TINYINT;
TOK_SMALLINT;
TOK_INT;
TOK_BIGINT;
TOK_BOOLEAN;
TOK_FLOAT;
TOK_DOUBLE;
TOK_DATE;
TOK_DATETIME;
TOK_TIMESTAMP;
TOK_STRING;
TOK_LIST;
TOK_MAP;
TOK_CREATETABLE;
TOK_DESCTABLE;
TOK_ALTERTABLE_RENAME;
TOK_ALTERTABLE_ADDCOLS;
TOK_ALTERTABLE_REPLACECOLS;
TOK_ALTERTABLE_ADDPARTS;
TOK_ALTERTABLE_DROPPARTS;
TOK_ALTERTABLE_SERDEPROPERTIES;
TOK_ALTERTABLE_SERIALIZER;
TOK_ALTERTABLE_PROPERTIES;
TOK_MSCK;
TOK_SHOWTABLES;
TOK_SHOWPARTITIONS;
TOK_CREATEEXTTABLE;
TOK_DROPTABLE;
TOK_TABCOLLIST;
TOK_TABCOL;
TOK_TABLECOMMENT;
TOK_TABLEPARTCOLS;
TOK_TABLEBUCKETS;
TOK_TABLEROWFORMAT;
TOK_TABLEROWFORMATFIELD;
TOK_TABLEROWFORMATCOLLITEMS;
TOK_TABLEROWFORMATMAPKEYS;
TOK_TABLEROWFORMATLINES;
TOK_TBLSEQUENCEFILE;
TOK_TBLTEXTFILE;
TOK_TABLEFILEFORMAT;
TOK_TABCOLNAME;
TOK_TABLELOCATION;
TOK_PARTITIONLOCATION;
TOK_TABLESAMPLE;
TOK_TMP_FILE;
TOK_TABSORTCOLNAMEASC;
TOK_TABSORTCOLNAMEDESC;
TOK_CHARSETLITERAL;
TOK_CREATEFUNCTION;
TOK_EXPLAIN;
TOK_TABLESERIALIZER;
TOK_TABLEPROPERTIES;
TOK_TABLEPROPLIST;
TOK_TABTYPE;
TOK_LIMIT;
TOK_TABLEPROPERTY;
TOK_IFNOTEXISTS;
}


// Package headers
@header {
package org.apache.hadoop.hive.ql.parse;
}
@lexer::header {package org.apache.hadoop.hive.ql.parse;}


@members { 
  Stack msgs = new Stack<String>();
}

@rulecatch {
catch (RecognitionException e) {
 reportError(e);
  throw e;
}
}
 
// starting rule
statement
	: explainStatement EOF
	| execStatement EOF
	;

explainStatement
@init { msgs.push("explain statement"); }
@after { msgs.pop(); }
	: KW_EXPLAIN (isExtended=KW_EXTENDED)? execStatement -> ^(TOK_EXPLAIN execStatement $isExtended?)
	;
		
execStatement
@init { msgs.push("statement"); }
@after { msgs.pop(); }
    : queryStatementExpression
    | loadStatement
    | ddlStatement
    ;

loadStatement
@init { msgs.push("load statement"); }
@after { msgs.pop(); }
    : KW_LOAD KW_DATA (islocal=KW_LOCAL)? KW_INPATH (path=StringLiteral) (isoverwrite=KW_OVERWRITE)? KW_INTO KW_TABLE (tab=tabName) 
    -> ^(TOK_LOAD $path $tab $islocal? $isoverwrite?)
    ;

ddlStatement
@init { msgs.push("ddl statement"); }
@after { msgs.pop(); }
    : createStatement
    | dropStatement
    | alterStatement
    | descStatement
    | showStatement
    | metastoreCheck
    | createFunctionStatement
    ;

ifNotExists
@init { msgs.push("if not exists clause"); }
@after { msgs.pop(); }
    : KW_IF KW_NOT KW_EXISTS
    -> ^(TOK_IFNOTEXISTS)
    ;

createStatement
@init { msgs.push("create statement"); }
@after { msgs.pop(); }
    : KW_CREATE (ext=KW_EXTERNAL)? KW_TABLE ifNotExists? name=Identifier (LPAREN columnNameTypeList RPAREN)? tableComment? tablePartition? tableBuckets? tableRowFormat? tableFileFormat? tableLocation?
    -> {$ext == null}? ^(TOK_CREATETABLE $name ifNotExists? columnNameTypeList? tableComment? tablePartition? tableBuckets? tableRowFormat? tableFileFormat? tableLocation?)
    ->                 ^(TOK_CREATEEXTTABLE $name ifNotExists? columnNameTypeList? tableComment? tablePartition? tableBuckets? tableRowFormat? tableFileFormat? tableLocation?)
    ;

dropStatement
@init { msgs.push("drop statement"); }
@after { msgs.pop(); }
    : KW_DROP KW_TABLE Identifier  -> ^(TOK_DROPTABLE Identifier)
    ;

alterStatement
@init { msgs.push("alter statement"); }
@after { msgs.pop(); }
    : alterStatementRename
    | alterStatementAddCol
    | alterStatementDropPartitions
    | alterStatementAddPartitions
    | alterStatementProperties
    | alterStatementSerdeProperties
    ;

alterStatementRename
@init { msgs.push("rename statement"); }
@after { msgs.pop(); }
    : KW_ALTER KW_TABLE oldName=Identifier KW_RENAME KW_TO newName=Identifier 
    -> ^(TOK_ALTERTABLE_RENAME $oldName $newName)
    ;

alterStatementAddCol
@init { msgs.push("add column statement"); }
@after { msgs.pop(); }
    : KW_ALTER KW_TABLE Identifier (add=KW_ADD | replace=KW_REPLACE) KW_COLUMNS LPAREN columnNameTypeList RPAREN
    -> {$add != null}? ^(TOK_ALTERTABLE_ADDCOLS Identifier columnNameTypeList)
    ->                 ^(TOK_ALTERTABLE_REPLACECOLS Identifier columnNameTypeList)
    ;

alterStatementAddPartitions
@init { msgs.push("add partition statement"); }
@after { msgs.pop(); }
    : KW_ALTER KW_TABLE Identifier KW_ADD partitionSpec partitionLocation? (partitionSpec partitionLocation?)*
    -> ^(TOK_ALTERTABLE_ADDPARTS Identifier (partitionSpec partitionLocation?)+)
    ;

partitionLocation
@init { msgs.push("partition location"); }
@after { msgs.pop(); }
    :
      KW_LOCATION locn=StringLiteral -> ^(TOK_PARTITIONLOCATION $locn)
    ;

alterStatementDropPartitions
@init { msgs.push("drop partition statement"); }
@after { msgs.pop(); }
    : KW_ALTER KW_TABLE Identifier KW_DROP partitionSpec (COMMA partitionSpec)*
    -> ^(TOK_ALTERTABLE_DROPPARTS Identifier partitionSpec+)
    ;

alterStatementProperties
@init { msgs.push("alter properties statement"); }
@after { msgs.pop(); }
    : KW_ALTER KW_TABLE name=Identifier KW_SET KW_PROPERTIES tableProperties
    -> ^(TOK_ALTERTABLE_PROPERTIES $name tableProperties)
    ;

alterStatementSerdeProperties
@init { msgs.push("alter serdes statement"); }
@after { msgs.pop(); }
    : KW_ALTER KW_TABLE name=Identifier KW_SET KW_SERDE serde=StringLiteral (KW_WITH KW_SERDEPROPERTIES tableProperties)?
    -> ^(TOK_ALTERTABLE_SERIALIZER $name $serde tableProperties?)
    | KW_ALTER KW_TABLE name=Identifier KW_SET KW_SERDEPROPERTIES tableProperties
    -> ^(TOK_ALTERTABLE_SERDEPROPERTIES $name tableProperties)
    ;

tabTypeExpr
@init { msgs.push("specifying table types"); }
@after { msgs.pop(); }

   : Identifier (DOT^ (Identifier | KW_ELEM_TYPE | KW_KEY_TYPE | KW_VALUE_TYPE))*
   ;
   
partTypeExpr
@init { msgs.push("specifying table partitions"); }
@after { msgs.pop(); }
    :  tabTypeExpr partitionSpec? -> ^(TOK_TABTYPE tabTypeExpr partitionSpec?)
    ;

descStatement
@init { msgs.push("describe statement"); }
@after { msgs.pop(); }
    : KW_DESCRIBE (isExtended=KW_EXTENDED)? (parttype=partTypeExpr)  -> ^(TOK_DESCTABLE $parttype $isExtended?)
    ;

showStatement
@init { msgs.push("show statement"); }
@after { msgs.pop(); }
    : KW_SHOW KW_TABLES showStmtIdentifier?  -> ^(TOK_SHOWTABLES showStmtIdentifier?)
    | KW_SHOW KW_PARTITIONS Identifier -> ^(TOK_SHOWPARTITIONS Identifier)
    ;
    
metastoreCheck
@init { msgs.push("metastore check statement"); }
@after { msgs.pop(); }
    : KW_MSCK (KW_TABLE table=Identifier partitionSpec? (COMMA partitionSpec)*)?
    -> ^(TOK_MSCK ($table partitionSpec*)?)
    ;     
    
createFunctionStatement
@init { msgs.push("create function statement"); }
@after { msgs.pop(); }
    : KW_CREATE KW_TEMPORARY KW_FUNCTION Identifier KW_AS StringLiteral
    -> ^(TOK_CREATEFUNCTION Identifier StringLiteral)
    ;

showStmtIdentifier
@init { msgs.push("identifier for show statement"); }
@after { msgs.pop(); }
    : Identifier
    | StringLiteral
    ;

tableComment
@init { msgs.push("table's comment"); }
@after { msgs.pop(); }
    :
      KW_COMMENT comment=StringLiteral  -> ^(TOK_TABLECOMMENT $comment)
    ;

tablePartition
@init { msgs.push("table partition specification"); }
@after { msgs.pop(); }
    : KW_PARTITIONED KW_BY LPAREN columnNameTypeList RPAREN 
    -> ^(TOK_TABLEPARTCOLS columnNameTypeList)
    ;

tableBuckets
@init { msgs.push("table buckets specification"); }
@after { msgs.pop(); }
    :
      KW_CLUSTERED KW_BY LPAREN bucketCols=columnNameList RPAREN (KW_SORTED KW_BY LPAREN sortCols=columnNameOrderList RPAREN)? KW_INTO num=Number KW_BUCKETS 
    -> ^(TOK_TABLEBUCKETS $bucketCols $sortCols? $num)
    ;

tableRowFormat
@init { msgs.push("table row format specification"); }
@after { msgs.pop(); }
    :
      KW_ROW KW_FORMAT KW_DELIMITED tableRowFormatFieldIdentifier? tableRowFormatCollItemsIdentifier? tableRowFormatMapKeysIdentifier? tableRowFormatLinesIdentifier? 
    -> ^(TOK_TABLEROWFORMAT tableRowFormatFieldIdentifier? tableRowFormatCollItemsIdentifier? tableRowFormatMapKeysIdentifier? tableRowFormatLinesIdentifier?)
    | KW_ROW KW_FORMAT KW_SERDE name=StringLiteral (KW_WITH KW_SERDEPROPERTIES serdeprops=tableProperties)?
    -> ^(TOK_TABLESERIALIZER $name $serdeprops?)
    ;

tableProperties
@init { msgs.push("table properties"); }
@after { msgs.pop(); }
    :
      LPAREN propertiesList RPAREN -> ^(TOK_TABLEPROPERTIES propertiesList)
    ;

propertiesList
@init { msgs.push("properties list"); }
@after { msgs.pop(); }
    :
      keyValueProperty (COMMA keyValueProperty)* -> ^(TOK_TABLEPROPLIST keyValueProperty+)
    ;

keyValueProperty
@init { msgs.push("specifying key/value property"); }
@after { msgs.pop(); }
    :
      key=StringLiteral EQUAL value=StringLiteral -> ^(TOK_TABLEPROPERTY $key $value)
    ;

tableRowFormatFieldIdentifier
@init { msgs.push("table row format's field separator"); }
@after { msgs.pop(); }
    :
      KW_FIELDS KW_TERMINATED KW_BY fldIdnt=StringLiteral 
    -> ^(TOK_TABLEROWFORMATFIELD $fldIdnt)
    ;

tableRowFormatCollItemsIdentifier
@init { msgs.push("table row format's column separator"); }
@after { msgs.pop(); }
    :
      KW_COLLECTION KW_ITEMS KW_TERMINATED KW_BY collIdnt=StringLiteral
    -> ^(TOK_TABLEROWFORMATCOLLITEMS $collIdnt)
    ;

tableRowFormatMapKeysIdentifier
@init { msgs.push("table row format's map key separator"); }
@after { msgs.pop(); }
    :
      KW_MAP KW_KEYS KW_TERMINATED KW_BY mapKeysIdnt=StringLiteral
    -> ^(TOK_TABLEROWFORMATMAPKEYS $mapKeysIdnt)
    ;

tableRowFormatLinesIdentifier
@init { msgs.push("table row format's line separator"); }
@after { msgs.pop(); }
    :
      KW_LINES KW_TERMINATED KW_BY linesIdnt=StringLiteral
    -> ^(TOK_TABLEROWFORMATLINES $linesIdnt)
    ;

tableFileFormat
@init { msgs.push("table file format specification"); }
@after { msgs.pop(); }
    :
      KW_STORED KW_AS KW_SEQUENCEFILE  -> TOK_TBLSEQUENCEFILE
      | KW_STORED KW_AS KW_TEXTFILE  -> TOK_TBLTEXTFILE
      | KW_STORED KW_AS KW_INPUTFORMAT inFmt=StringLiteral KW_OUTPUTFORMAT outFmt=StringLiteral
      -> ^(TOK_TABLEFILEFORMAT $inFmt $outFmt)
    ;

tableLocation
@init { msgs.push("table location specification"); }
@after { msgs.pop(); }
    :
      KW_LOCATION locn=StringLiteral -> ^(TOK_TABLELOCATION $locn)
    ;
  
columnNameTypeList
@init { msgs.push("column name type list"); }
@after { msgs.pop(); }
    : columnNameType (COMMA columnNameType)* -> ^(TOK_TABCOLLIST columnNameType+)
    ;

columnNameList
@init { msgs.push("column name list"); }
@after { msgs.pop(); }
    : columnName (COMMA columnName)* -> ^(TOK_TABCOLNAME columnName+)
    ;

columnName
@init { msgs.push("column name"); }
@after { msgs.pop(); }
    :
      Identifier
    ;

columnNameOrderList
@init { msgs.push("column name order list"); }
@after { msgs.pop(); }
    : columnNameOrder (COMMA columnNameOrder)* -> ^(TOK_TABCOLNAME columnNameOrder+)
    ;

columnNameOrder
@init { msgs.push("column name order"); }
@after { msgs.pop(); }
    : Identifier (asc=KW_ASC | desc=KW_DESC)? 
    -> {$desc == null}? ^(TOK_TABSORTCOLNAMEASC Identifier)
    ->                  ^(TOK_TABSORTCOLNAMEDESC Identifier)
    ;

columnRefOrder
@init { msgs.push("column order"); }
@after { msgs.pop(); }
    : tableColumn (asc=KW_ASC | desc=KW_DESC)? 
    -> {$desc == null}? ^(TOK_TABSORTCOLNAMEASC tableColumn)
    ->                  ^(TOK_TABSORTCOLNAMEDESC tableColumn)
    ;

columnNameType
@init { msgs.push("column specification"); }
@after { msgs.pop(); }
    : colName=Identifier colType (KW_COMMENT comment=StringLiteral)?    
    -> {$comment == null}? ^(TOK_TABCOL $colName colType)
    ->                     ^(TOK_TABCOL $colName colType $comment)
    ;

colType
@init { msgs.push("column type"); }
@after { msgs.pop(); }
    : primitiveType
    | listType
    | mapType
    ;

primitiveType
@init { msgs.push("primitive type specification"); }
@after { msgs.pop(); }
    : KW_TINYINT       ->    TOK_TINYINT
    | KW_SMALLINT      ->    TOK_SMALLINT
    | KW_INT           ->    TOK_INT
    | KW_BIGINT        ->    TOK_BIGINT
    | KW_BOOLEAN       ->    TOK_BOOLEAN
    | KW_FLOAT         ->    TOK_FLOAT
    | KW_DOUBLE        ->    TOK_DOUBLE
    | KW_DATE          ->    TOK_DATE
    | KW_DATETIME      ->    TOK_DATETIME
    | KW_TIMESTAMP     ->    TOK_TIMESTAMP
    | KW_STRING        ->    TOK_STRING
    ;

listType
@init { msgs.push("list type"); }
@after { msgs.pop(); }
    : KW_ARRAY LESSTHAN primitiveType GREATERTHAN   -> ^(TOK_LIST primitiveType)
    ;

mapType
@init { msgs.push("map type"); }
@after { msgs.pop(); }
    : KW_MAP LESSTHAN left=primitiveType COMMA right=primitiveType GREATERTHAN
    -> ^(TOK_MAP $left $right)
    ;

queryOperator
@init { msgs.push("query operator"); }
@after { msgs.pop(); }
    : KW_UNION KW_ALL -> ^(TOK_UNION)
    ;

// select statement select ... from ... where ... group by ... order by ...
queryStatementExpression
    : queryStatement (queryOperator^ queryStatementExpression)*
    ;

queryStatement
    :
    fromClause
    ( b+=body )+ -> ^(TOK_QUERY fromClause body+)
    | regular_body
    ;

regular_body
   :
   insertClause
   selectClause
   fromClause
   whereClause?
   groupByClause?
   orderByClause?
   clusterByClause?
   distributeByClause?
   sortByClause?
   limitClause? -> ^(TOK_QUERY fromClause ^(TOK_INSERT insertClause
                     selectClause whereClause? groupByClause? orderByClause? clusterByClause?
                     distributeByClause? sortByClause? limitClause?))
   |
   selectClause
   fromClause
   whereClause?
   groupByClause?
   orderByClause?
   clusterByClause?
   distributeByClause?
   sortByClause?
   limitClause? -> ^(TOK_QUERY fromClause ^(TOK_INSERT ^(TOK_DESTINATION ^(TOK_DIR TOK_TMP_FILE))
                     selectClause whereClause? groupByClause? orderByClause? clusterByClause?
                     distributeByClause? sortByClause? limitClause?))
   ;


body
   :
   insertClause
   selectClause
   whereClause?
   groupByClause?
   orderByClause?
   clusterByClause?
   distributeByClause?
   sortByClause?
   limitClause? -> ^(TOK_INSERT insertClause?
                     selectClause whereClause? groupByClause? orderByClause? clusterByClause?
                     distributeByClause? sortByClause? limitClause?)
   |
   selectClause
   whereClause?
   groupByClause?
   orderByClause?
   clusterByClause?
   distributeByClause?
   sortByClause?
   limitClause? -> ^(TOK_INSERT ^(TOK_DESTINATION ^(TOK_DIR TOK_TMP_FILE))
                     selectClause whereClause? groupByClause? orderByClause? clusterByClause?
                     distributeByClause? sortByClause? limitClause?)
   ;

insertClause
@init { msgs.push("insert clause"); }
@after { msgs.pop(); }
   :
   KW_INSERT KW_OVERWRITE destination -> ^(TOK_DESTINATION destination)
   ;

destination
@init { msgs.push("destination specification"); }
@after { msgs.pop(); }
   :
     KW_LOCAL KW_DIRECTORY StringLiteral -> ^(TOK_LOCAL_DIR StringLiteral)
   | KW_DIRECTORY StringLiteral -> ^(TOK_DIR StringLiteral)
   | KW_TABLE tabName -> ^(tabName)
   ;

limitClause
@init { msgs.push("limit clause"); }
@after { msgs.pop(); }
   :
   KW_LIMIT num=Number -> ^(TOK_LIMIT $num)
   ;

//----------------------- Rules for parsing selectClause -----------------------------
// select a,b,c ...
selectClause
@init { msgs.push("select clause"); }
@after { msgs.pop(); }
    :
    KW_SELECT (KW_ALL | dist=KW_DISTINCT)?
    selectList -> {$dist == null}? ^(TOK_SELECT selectList)
               ->                  ^(TOK_SELECTDI selectList)
    |
    KW_SELECT KW_TRANSFORM trfmClause -> ^(TOK_SELECT ^(TOK_SELEXPR trfmClause) )
    |
    KW_MAP trfmClause -> ^(TOK_SELECT ^(TOK_SELEXPR trfmClause) )
    |
    KW_REDUCE trfmClause -> ^(TOK_SELECT ^(TOK_SELEXPR trfmClause) )
    ;

selectList
@init { msgs.push("select list"); }
@after { msgs.pop(); }
    :
    selectItem ( COMMA  selectItem )* -> selectItem+
    ;

selectItem
@init { msgs.push("selection target"); }
@after { msgs.pop(); }
    :
    ( selectExpression  (KW_AS Identifier)?) -> ^(TOK_SELEXPR selectExpression Identifier?)
    ;
    
trfmClause
@init { msgs.push("transform clause"); }
@after { msgs.pop(); }
    :
    ( LPAREN selectExpressionList RPAREN | selectExpressionList )
    KW_USING StringLiteral
    (KW_AS (LPAREN aliasList RPAREN | aliasList) )?
    -> ^(TOK_TRANSFORM selectExpressionList StringLiteral aliasList?)
    ;
    
selectExpression
@init { msgs.push("select expression"); }
@after { msgs.pop(); }
    :
    expression | tableAllColumns
    ;

selectExpressionList
@init { msgs.push("select expression list"); }
@after { msgs.pop(); }
    :
    selectExpression (COMMA selectExpression)* -> ^(TOK_EXPLIST selectExpression+)
    ;


//-----------------------------------------------------------------------------------

tableAllColumns
    :
    STAR -> ^(TOK_ALLCOLREF)
    | Identifier DOT STAR -> ^(TOK_ALLCOLREF Identifier)
    ;
    
// table.column
tableColumn
@init { msgs.push("table column identifier"); }
@after { msgs.pop(); }
    :
    (tab=Identifier  DOT)? col=Identifier -> ^(TOK_COLREF $tab? $col)
    ;

expressionList
@init { msgs.push("expression list"); }
@after { msgs.pop(); }
    :
    expression (COMMA expression)* -> ^(TOK_EXPLIST expression+)
    ;

aliasList
@init { msgs.push("alias list"); }
@after { msgs.pop(); }
    :
    Identifier (COMMA Identifier)* -> ^(TOK_ALIASLIST Identifier+)
    ;
   
//----------------------- Rules for parsing fromClause ------------------------------
// from [col1, col2, col3] table1, [col4, col5] table2
fromClause
@init { msgs.push("from clause"); }
@after { msgs.pop(); }
    :
      KW_FROM joinSource -> ^(TOK_FROM joinSource)
    | KW_FROM fromSource -> ^(TOK_FROM fromSource)
    ;

joinSource    
@init { msgs.push("join source"); }
@after { msgs.pop(); }
    :
    fromSource 
    ( joinToken^ fromSource (KW_ON! expression)? )+
    ;

joinToken
@init { msgs.push("join type specifier"); }
@after { msgs.pop(); }
    :
      KW_JOIN                     -> TOK_JOIN
    | KW_LEFT KW_OUTER KW_JOIN    -> TOK_LEFTOUTERJOIN
    | KW_RIGHT KW_OUTER KW_JOIN   -> TOK_RIGHTOUTERJOIN
    | KW_FULL KW_OUTER KW_JOIN    -> TOK_FULLOUTERJOIN
    ;

fromSource
@init { msgs.push("from source"); }
@after { msgs.pop(); }
    :
    (tableSource | subQuerySource)
    ;
    
tableSample
@init { msgs.push("table sample specification"); }
@after { msgs.pop(); }
    :
    KW_TABLESAMPLE LPAREN KW_BUCKET (numerator=Number) KW_OUT KW_OF (denominator=Number) (KW_ON expr+=expression (COMMA expr+=expression)*)? RPAREN -> ^(TOK_TABLESAMPLE $numerator $denominator $expr*)
    ;

tableSource
@init { msgs.push("table source"); }
@after { msgs.pop(); }
    :
    tabname=Identifier (ts=tableSample)? (alias=Identifier)? -> ^(TOK_TABREF $tabname $ts? $alias?)
 
    ;

subQuerySource
@init { msgs.push("subquery source"); }
@after { msgs.pop(); }
    :
    LPAREN queryStatementExpression RPAREN Identifier -> ^(TOK_SUBQUERY queryStatementExpression Identifier)
    ;
        
//----------------------- Rules for parsing whereClause -----------------------------
// where a=b and ...
whereClause
@init { msgs.push("where clause"); }
@after { msgs.pop(); }
    :
    KW_WHERE searchCondition -> ^(TOK_WHERE searchCondition)
    ;

searchCondition
@init { msgs.push("search condition"); }
@after { msgs.pop(); }
    :
    expression
    ;

//-----------------------------------------------------------------------------------

// group by a,b
groupByClause
@init { msgs.push("group by clause"); }
@after { msgs.pop(); }
    :
    KW_GROUP KW_BY
    groupByExpression
    ( COMMA groupByExpression )*
    -> ^(TOK_GROUPBY groupByExpression+)
    ;

groupByExpression
@init { msgs.push("group by expression"); }
@after { msgs.pop(); }
    :
    expression
    ;

// order by a,b
orderByClause
@init { msgs.push("order by clause"); }
@after { msgs.pop(); }
    :
    KW_ORDER KW_BY
    orderByExpression
    ( COMMA orderByExpression )*
    -> ^(TOK_ORDERBY orderByExpression+)
    ;

orderByExpression
@init { msgs.push("order by expression"); }
@after { msgs.pop(); }
    :
    expression
    (KW_ASC | KW_DESC)?
    ;

clusterByClause
@init { msgs.push("cluster by clause"); }
@after { msgs.pop(); }
    :
    KW_CLUSTER KW_BY
    tableColumn
    ( COMMA tableColumn )* -> ^(TOK_CLUSTERBY tableColumn+)
    ;

distributeByClause
@init { msgs.push("distribute by clause"); }
@after { msgs.pop(); }
    :
    KW_DISTRIBUTE KW_BY
    expression (COMMA expression)* -> ^(TOK_DISTRIBUTEBY expression+)
    ;

sortByClause
@init { msgs.push("sort by clause"); }
@after { msgs.pop(); }
    :
    KW_SORT KW_BY
    columnRefOrder
    ( COMMA columnRefOrder)* -> ^(TOK_SORTBY columnRefOrder+)
    ;

// fun(par1, par2, par3)
function
@init { msgs.push("function specification"); }
@after { msgs.pop(); }
    :
    functionName
    LPAREN (
          ((dist=KW_DISTINCT)?
           expression
           (COMMA expression)*)?
        )?
    RPAREN -> {$dist == null}? ^(TOK_FUNCTION functionName (expression+)?)
                          -> ^(TOK_FUNCTIONDI functionName (expression+)?)

    ;

functionName
@init { msgs.push("function name"); }
@after { msgs.pop(); }
    : // Keyword IF is also a function name
    Identifier | KW_IF
    ;

castExpression
@init { msgs.push("cast expression"); }
@after { msgs.pop(); }
    :
    KW_CAST
    LPAREN 
          expression
          KW_AS
          primitiveType
    RPAREN -> ^(TOK_FUNCTION primitiveType expression)
    ;
    
constant
@init { msgs.push("constant"); }
@after { msgs.pop(); }
    :
    Number
    | StringLiteral
    | charSetStringLiteral
    | booleanValue 
    ;

charSetStringLiteral
@init { msgs.push("character string literal"); }
@after { msgs.pop(); }
    :
    csName=CharSetName csLiteral=CharSetLiteral -> ^(TOK_CHARSETLITERAL $csName $csLiteral)
    ;

expression
@init { msgs.push("expression specification"); }
@after { msgs.pop(); }
    :
    precedenceOrExpression
    ;

atomExpression
    :
    KW_NULL -> TOK_NULL
    | constant
    | function
    | castExpression
    | tableColumn
    | LPAREN! expression RPAREN!
    ;


precedenceFieldExpression
    :
    atomExpression ((LSQUARE^ expression RSQUARE!) | (DOT^ Identifier))*
    ;

precedenceUnaryOperator
    :
    PLUS | MINUS | TILDE
    ;

precedenceUnaryExpression
    :
      precedenceFieldExpression KW_IS KW_NULL -> ^(TOK_FUNCTION TOK_ISNULL precedenceFieldExpression)
    | precedenceFieldExpression KW_IS KW_NOT KW_NULL -> ^(TOK_FUNCTION TOK_ISNOTNULL precedenceFieldExpression)
    | (precedenceUnaryOperator^)* precedenceFieldExpression
    ;


precedenceBitwiseXorOperator
    :
    BITWISEXOR
    ;

precedenceBitwiseXorExpression
    :
    precedenceUnaryExpression (precedenceBitwiseXorOperator^ precedenceUnaryExpression)*
    ;

	
precedenceStarOperator
    :
    STAR | DIVIDE | MOD
    ;

precedenceStarExpression
    :
    precedenceBitwiseXorExpression (precedenceStarOperator^ precedenceBitwiseXorExpression)*
    ;


precedencePlusOperator
    :
    PLUS | MINUS
    ;

precedencePlusExpression
    :
    precedenceStarExpression (precedencePlusOperator^ precedenceStarExpression)*
    ;


precedenceAmpersandOperator
    :
    AMPERSAND
    ;

precedenceAmpersandExpression
    :
    precedencePlusExpression (precedenceAmpersandOperator^ precedencePlusExpression)*
    ;


precedenceBitwiseOrOperator
    :
    BITWISEOR
    ;

precedenceBitwiseOrExpression
    :
    precedenceAmpersandExpression (precedenceBitwiseOrOperator^ precedenceAmpersandExpression)*
    ;


precedenceEqualOperator
    :
    EQUAL | NOTEQUAL | LESSTHANOREQUALTO | LESSTHAN | GREATERTHANOREQUALTO | GREATERTHAN
    | KW_LIKE | KW_RLIKE | KW_REGEXP
    ;

precedenceEqualExpression
    :
    precedenceBitwiseOrExpression (precedenceEqualOperator^ precedenceBitwiseOrExpression)*
    ;


precedenceNotOperator
    :
    KW_NOT
    ;

precedenceNotExpression
    :
    (precedenceNotOperator^)* precedenceEqualExpression
    ;


precedenceAndOperator
    :
    KW_AND
    ;

precedenceAndExpression
    :
    precedenceNotExpression (precedenceAndOperator^ precedenceNotExpression)*
    ;


precedenceOrOperator
    :
    KW_OR
    ;

precedenceOrExpression
    :
    precedenceAndExpression (precedenceOrOperator^ precedenceAndExpression)*
    ;


booleanValue
    :
    KW_TRUE^ | KW_FALSE^
    ;

tabName
   :
   Identifier partitionSpec? -> ^(TOK_TAB Identifier partitionSpec?)
   ;
        
partitionSpec
    :
    KW_PARTITION
     LPAREN partitionVal (COMMA  partitionVal )* RPAREN -> ^(TOK_PARTSPEC partitionVal +)
    ;

partitionVal
    :
    Identifier EQUAL constant -> ^(TOK_PARTVAL Identifier constant)
    ;    

// Keywords
KW_TRUE : 'TRUE';
KW_FALSE : 'FALSE';
KW_ALL : 'ALL';
KW_AND : 'AND';
KW_OR : 'OR';
KW_NOT : 'NOT';
KW_LIKE : 'LIKE';

KW_IF : 'IF';
KW_EXISTS : 'EXISTS';

KW_ASC : 'ASC';
KW_DESC : 'DESC';
KW_ORDER : 'ORDER';
KW_BY : 'BY';
KW_GROUP : 'GROUP';
KW_WHERE : 'WHERE';
KW_FROM : 'FROM';
KW_AS : 'AS';
KW_SELECT : 'SELECT';
KW_DISTINCT : 'DISTINCT';
KW_INSERT : 'INSERT';
KW_OVERWRITE : 'OVERWRITE';
KW_OUTER : 'OUTER';
KW_JOIN : 'JOIN';
KW_LEFT : 'LEFT';
KW_RIGHT : 'RIGHT';
KW_FULL : 'FULL';
KW_ON : 'ON';
KW_PARTITION : 'PARTITION';
KW_PARTITIONS : 'PARTITIONS';
KW_TABLE: 'TABLE';
KW_TABLES: 'TABLES';
KW_SHOW: 'SHOW';
KW_MSCK: 'MSCK';
KW_DIRECTORY: 'DIRECTORY';
KW_LOCAL: 'LOCAL';
KW_TRANSFORM : 'TRANSFORM';
KW_USING: 'USING';
KW_CLUSTER: 'CLUSTER';
KW_DISTRIBUTE: 'DISTRIBUTE';
KW_SORT: 'SORT';
KW_UNION: 'UNION';
KW_LOAD: 'LOAD';
KW_DATA: 'DATA';
KW_INPATH: 'INPATH';
KW_IS: 'IS';
KW_NULL: 'NULL';
KW_CREATE: 'CREATE';
KW_EXTERNAL: 'EXTERNAL';
KW_ALTER: 'ALTER';
KW_DESCRIBE: 'DESCRIBE';
KW_DROP: 'DROP';
KW_RENAME: 'RENAME';
KW_TO: 'TO';
KW_COMMENT: 'COMMENT';
KW_BOOLEAN: 'BOOLEAN';
KW_TINYINT: 'TINYINT';
KW_SMALLINT: 'SMALLINT';
KW_INT: 'INT';
KW_BIGINT: 'BIGINT';
KW_FLOAT: 'FLOAT';
KW_DOUBLE: 'DOUBLE';
KW_DATE: 'DATE';
KW_DATETIME: 'DATETIME';
KW_TIMESTAMP: 'TIMESTAMP';
KW_STRING: 'STRING';
KW_ARRAY: 'ARRAY';
KW_MAP: 'MAP';
KW_REDUCE: 'REDUCE';
KW_PARTITIONED: 'PARTITIONED';
KW_CLUSTERED: 'CLUSTERED';
KW_SORTED: 'SORTED';
KW_INTO: 'INTO';
KW_BUCKETS: 'BUCKETS';
KW_ROW: 'ROW';
KW_FORMAT: 'FORMAT';
KW_DELIMITED: 'DELIMITED';
KW_FIELDS: 'FIELDS';
KW_TERMINATED: 'TERMINATED';
KW_COLLECTION: 'COLLECTION';
KW_ITEMS: 'ITEMS';
KW_KEYS: 'KEYS';
KW_KEY_TYPE: '$KEY$';
KW_LINES: 'LINES';
KW_STORED: 'STORED';
KW_SEQUENCEFILE: 'SEQUENCEFILE';
KW_TEXTFILE: 'TEXTFILE';
KW_INPUTFORMAT: 'INPUTFORMAT';
KW_OUTPUTFORMAT: 'OUTPUTFORMAT';
KW_LOCATION: 'LOCATION';
KW_TABLESAMPLE: 'TABLESAMPLE';
KW_BUCKET: 'BUCKET';
KW_OUT: 'OUT';
KW_OF: 'OF';
KW_CAST: 'CAST';
KW_ADD: 'ADD';
KW_REPLACE: 'REPLACE';
KW_COLUMNS: 'COLUMNS';
KW_RLIKE: 'RLIKE';
KW_REGEXP: 'REGEXP';
KW_TEMPORARY: 'TEMPORARY';
KW_FUNCTION: 'FUNCTION';
KW_EXPLAIN: 'EXPLAIN';
KW_EXTENDED: 'EXTENDED';
KW_SERDE: 'SERDE';
KW_WITH: 'WITH';
KW_SERDEPROPERTIES: 'SERDEPROPERTIES';
KW_LIMIT: 'LIMIT';
KW_SET: 'SET';
KW_PROPERTIES: 'TBLPROPERTIES';
KW_VALUE_TYPE: '$VALUE$';
KW_ELEM_TYPE: '$ELEM$';

// Operators

DOT : '.'; // generated as a part of Number rule
COLON : ':' ;
COMMA : ',' ;
SEMICOLON : ';' ;

LPAREN : '(' ;
RPAREN : ')' ;
LSQUARE : '[' ;
RSQUARE : ']' ;

EQUAL : '=';
NOTEQUAL : '<>';
LESSTHANOREQUALTO : '<=';
LESSTHAN : '<';
GREATERTHANOREQUALTO : '>=';
GREATERTHAN : '>';

DIVIDE : '/';
PLUS : '+';
MINUS : '-';
STAR : '*';
MOD : '%';

AMPERSAND : '&';
TILDE : '~';
BITWISEOR : '|';
BITWISEXOR : '^';

// LITERALS
fragment
Letter
    : 'a'..'z' | 'A'..'Z'
    ;

fragment
HexDigit
    : 'a'..'f' | 'A'..'F' 
    ;

fragment
Digit
    :
    '0'..'9'
    ;

fragment
Exponent
    :
    'e' ( PLUS|MINUS )? (Digit)+
    ;

StringLiteral
    :
    ( '\'' (~'\'')* '\'' | '\"' (~'\"')* '\"' )+
    ;

CharSetLiteral
    :    
    StringLiteral 
    | '0' 'X' (HexDigit|Digit)+
    ;

Number
    :
    (Digit)+ ( DOT (Digit)* (Exponent)? | Exponent)?
    ;

Identifier
    :
    (Letter | Digit) (Letter | Digit | '_')*
    | '`' (Letter | Digit) (Letter | Digit | '_')* '`'
    ;

CharSetName
    :
    '_' (Letter | Digit | '_' | '-' | '.' | ':' )+
    ;

WS  :  (' '|'\r'|'\t'|'\n') {$channel=HIDDEN;}
    ;

COMMENT
  : '--' (~('\n'|'\r'))*
    { $channel=HIDDEN; }
  ;


