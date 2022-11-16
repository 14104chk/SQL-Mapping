ResultSet Mapper 是一个轻量的SQL结果集映射工具。

例如：
```java
package ssll.rsm;

public class ZClass {
	private int id;
	private String name;
	private List<Student> students;
	...
}

public class Student {
	private int id;
	private int classId;
	private String name;
	...
}
```

表：t_class
| id | name |
| :----: | :----: |
| 1 | Class-A |
| 2 | Class-B |
| 3 | Class-C |

表：t_student
| id | class_id | name |
| :----: | :----: | :----: |
| 1 | 1 | Student-a1 |
| 2 | 1 | Student-a2 |
| 3 | 2 | Student-b |

使用如下的SQL:
```sql
SELECT NULL AS ':entity(ssll.rsm.ZClass):id(id)', s1.*, NULL AS '.students:list:entity(ssll.rsm.Student):id(id)', s2.*
FROM t_class s1 LEFT JOIN t_student s2 ON s2.class_id=s1.id
```
映射结果集
```java
MappingContext context = new MappingContext();
List<ZClass> list = (List<ZClass>) context.mapping(/* ResultSet */rs);
```
可以得到如下结果:
```json
[
	{
		"id": 1,
		"name": "Class-A",
		"students": [
			{
				"id": 1,
				"classId": 1,
				"name": "Student-a1"
			},
			{
				"id": 2,
				"classId": 1,
				"name": "Student-a2"
			}
		]
	},
	{
		"id": 2,
		"name": "Class-B",
		"students": [
			{
				"id": 3,
				"classId": 2,
				"name": "Student-b"
			}
		]
	},
	{
		"id": 3,
		"name": "Class-C",
		"students": null
	}
]
```
