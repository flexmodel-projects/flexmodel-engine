# JsonLogic

通过Json构建复杂的dsl语法，适用于低代码开发平台的条件规则和表达式等功能

## Basic Specification

```json
{
  "operator": [
    "values",
    "..."
  ]
}
```

## Node Type

| 类型               | 结构定义                                         | 说明                                                               | 示例                                     |
|------------------|----------------------------------------------|------------------------------------------------------------------|----------------------------------------|
| var              | `{"var":[<variable_name> (,default_value) }` | 变量,arg1: 变量名称, arg2: 默认值，非必填                                     | {"var":["foo",1]} 或者 {"var": "foo" }   |  
| table_field      | `{"table_field":[<table>,<field>]}`          | arg1: table, arg2: field                                         | {"table_field":["user","name"]}        |
| datetime         | ` {"datetime":"2021-09-02T02:50:12.208"}`    | 日期时间, 时间格式yyyy-MM-dd'T'HH:mm:ss.SSS                              | {"datetime":"2021-09-02T02:50:12.208"} | 
| temporal_offset  | `{"temporal_offset":[<unit>,<offset>]} `     | 时间偏移, arg1: `year` `month` `week` `day` `hour` `minute` `second` | {"temporal_offset":["year",1]}         | 
| current_datetime | `{"current_datetime":[]} `                   | 当前时间                                                             | {"current_datetime":[]}                | 
| current_user     | `{"current_user":[]} `                       | 当前用户                                                             | {"current_user":[]}                    | 

## Operator

| 操作符          | 说明   | 可运算值类型                |
|--------------|------|-----------------------|
| and          | 与    | boolean               |
| or           | 或    | boolean               |
| ==           | 等于   | number+string+boolean |
| !=           | 不等于  | number+string+boolean |
| >            | 大于   | number                |
| >=           | 大于等于 | number                |
| <            | 小于   | number                |
| <=           | 小于等于 | number                |
| +            | 加    | number                |
| -            | 减    | number                |
| *            | 乘    | number                |
| /            | 除    | number                |
| %            | 求余   | number                |
| contains     | 包含   | array+string          |
| not_contains | 不包含  | array+string          |
| between      | 范围   | datetime+number       |

## Functions

### 字符串函数

#### UPPER(string)

说明：返回转换为大写的字符串

示例：{"upper":"Who Am I"}

输出：WHO AM I

#### LOWER(string)

说明：返回转换为小写的字符串

示例：{"lower": "How Are You"}

输出： how are you

#### SUBSTR(string,pos[,len])

说明：字符串截取

pos: 截取坐标

len: 可选参数, 截取长度

示例一 ：{"substr":["中国经济航船行稳致远",2]}

输出：经济航船行稳致远

示例二：{"substr":["中国经济航船行稳致远",2,2]}

输出：经济

#### CAT(string[,string...])

说明：字符串拼接

示例：{"cat":["中国经济","航船","行稳致远"]}

输出：中国经济航船行稳致远

### 数值函数

#### MIN(number[,number...])

说明：计算最小值

示例：{"min":[1,3,5,1]}

输出：1

#### MAX(number[,number...])

说明：计算最大值

示例：{"max":[1,3,5,1]}

输出：5

### 聚合函数

说明：聚合函数适用于计算子表字段的数据值

#### COUNT(table_field)

说明：返回记录总数

示例：{"count": [{"table_field":["student","name"]}]}

#### AVG(table_field)

示例：{"avg": [{"table_field":["student","score"]}]}

说明：返回所有数值的平均值(算术平均值)

#### MIN(table_field)

说明：计算最小值

示例：{"min": [{"table_field":["student","age"]}]}

#### MAX(table_field)

说明：计算最大值

示例：{"max": [{"table_field":["student","age"]}]}

#### SUM(table_field)

说明：返回所有数值之和

示例：{"sum": [{"table_field":["student","score"]}]}

## Sql Logic Rules

支持的功能类型： `table_field` `var`

支持的表达式： `and` `or` `==` `!=` `>` `>=` `<` `<=` `contains` `not-contains` `between`

Example

```shell

json: {"and":[{">":[{"table_field":["user", "id"]}, 2]},{"==":["jack", {"table_field":["user", "name"]}]},{"<":[{"table_field":["user", "age"]}, 21]}]}

// 支持无限层级and/or嵌套

result:  ( user.id > ? and ? = user.name and user.age < ? )  

// 参数 [2,"jack",21]

```

## boolean Logic Rules

支持的功能类型： `var` `table_field` `datetime`

支持的表达式： `and` `or` `==` `!=` `>` `>=` `<` `<=` `contains` `not_contains` `between`

Example 1

```shell

json: {"or":[{">":[1, 2]},{"<":[1, 2]}]} 

// (1 > 2 or 1 < 2)

data: null

result: false

```

Example 2

```shell

json: {"and":[{"and":[{">=":[3, 5]},{"<=":[1, 2]}]},{"or":[{">":[3, 2]},{"<":[1, 2]}]}]}

 // ((3 >= 5 and 1 <= 2) and (3 > 2) or (1 < 2))

data : null

result: true
```

Example 3

```shell

json: {"and":[{">":[{"var":["a",3]}, 2]},{"<":[1, {"var":"b"}]},{"<":[{"var":"c.cc"},21]}]}

// ((a > 2) and (1 < b)) and c.cc < 21

data: {"b":10,"c":{"cc":20}} //a默认值是3

result: true

```

## Math Logic Rules

> 算术四则运算以及两个日期相减

支持的功能类型： `var` `datetime`

支持的表达式： `+` `-` `*` `/` `%`

Example

```shell

json: { "+": [ 5, { "%": [ { "/": [ { "*": [ 2 , { "-": [ 10, 6 ] } ] }, { "var": "a" } ] }, 2 ] } ] }

// 5 + (2 * ( 10 - 6 )/ 4 ) % 2 

data: { a: 4 }

result: 6

```

## Datetime Logic Rules

> 加或者减去某个日期时间

支持的功能类型：`var` `datetime` `temporal_offset`

支持的表达式： `+` `-`

Example

```shell

json: { "+": [ { "datetime": "2021-09-02T02:50:12.208" }, { "temporal_offset": [ "year", { "var": "a" } ] } ] }

data: { a: 1 }

result: 2022-09-02T02:50:12.208Z

```
