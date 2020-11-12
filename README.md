ResultSet Mapper 是一个轻量的SQL结果集映射工具。

例如：
```java
public class Class {
	private int id;
	private String name;
	private List<Student> students;
	...
}

public class Student {
	private int id;
	private String name;
	...
}
```
使用如下的SQL:
```sql
SELECT NULL AS ':ENTITY(Class)', s1.*, NULL AS '.students:LIST:ENTITY(Student)', s2.*
FROM Class s1 LEFT JOIN Student s2 ON s2.class_id=s1.id
```
映射结果集
```java
MappingConfig cfg = MappingConfig.getDefault();
MappingContext context = new MappingContext();
context.setConfig(cfg);
List<Class> list = (List<Class>) context.mapping(/* ResultSet */rs);
```
可以得到如下结果:
```json
[
	{
		"id": 1,
		"name": "Class-A",
		"students": [{
				"id": 1,
				"name": "Student-a1"
			}, {
				"id": 2,
				"name": "Student-a2"
			}]
	}, {
		"id": 2,
		"name": "Class-B",
		"students": [{
				"id": 3,
				"name": "Student-b"
			}]
	}
]
```
