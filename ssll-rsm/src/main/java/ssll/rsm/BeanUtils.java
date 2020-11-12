package ssll.rsm;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;
import static ssll.core.LangUitls.putIfAbsent;

public class BeanUtils {

	protected Pattern P_COLUMN = Pattern.compile("_(\\w)");
	protected ConcurrentMap<Object, PropertyDescriptor> propertyDescriptorCaches = new ConcurrentHashMap<>();
	protected ConcurrentMap<Object, Class> propertyActualTypeCaches = new ConcurrentHashMap<>();
	protected ConcurrentMap<Object, String> propertyNameCaches = new ConcurrentHashMap<>();
	protected MappingConfig config;

	public void flush() {
		Introspector.flushCaches();
		propertyDescriptorCaches.clear();
		propertyActualTypeCaches.clear();
		propertyNameCaches.clear();
	}

	protected void checkEntity(Object entity) throws NoSuchPropertyException {
		if (entity instanceof Map) {
			throw new NoSuchPropertyException();
		}
	}

	protected PropertyDescriptor findPropertyDescriptor(Object entity, String propertyName) throws NoSuchPropertyException {
		checkEntity(entity);
		try {
			Class<?> clazz = entity.getClass();
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			String key = clazz.getName() + ":" + propertyName;
			PropertyDescriptor value = propertyDescriptorCaches.get(key);
			if (value == null) {
				for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
					if (pd.getName().equals(propertyName)) {
						value = pd;
						break;
					}
				}
			}
			if (value == null) {
				throw new NoSuchPropertyException(String.format("[%s] no such property:%s", clazz, propertyName));
			}
			value = putIfAbsent(propertyDescriptorCaches, key, value);
			return value;
		} catch (IntrospectionException ex) {
			throw new NoSuchPropertyException(ex);
		}
	}

	public String findAvailablePropertyName(Object entity, Property property) {
		if (entity instanceof Map) {
			return property.getName();
		}
		String key = entity.getClass().getName() + ":" + property.getName();
		String propertyName = propertyNameCaches.get(key);
		if (propertyName != null) {
			return propertyName;
		}
		a:
		for (String name : new HashSet<>(Arrays.asList(property.getName(), property.getName().toLowerCase()))) {
			for (int i = 0;; i++) {
				switch (i) {
					case 0:
						propertyName = name;
						break;
					case 1:
						Matcher m = P_COLUMN.matcher(name);
						if (m.find()) {
							StringBuffer sb = new StringBuffer();
							do {
								m.appendReplacement(sb, m.group(1).toUpperCase());
							} while (m.find());
							m.appendTail(sb);
							propertyName = sb.toString();
						} else {
							continue;
						}
						break;
					default:
						continue a;
				}
				try {
					findPropertyDescriptor(entity, propertyName);
					return putIfAbsent(propertyNameCaches, key, propertyName);
				} catch (NoSuchPropertyException ex) {
				}
			}
		}
		return null;
	}

	public Class findPropertyActualType(Object entity, String propertyName) throws NoSuchPropertyException {
		checkEntity(entity);
		String key = entity.getClass().getName() + ":" + propertyName;
		Class clazz = propertyActualTypeCaches.get(key);
		if (clazz == null) {
			PropertyDescriptor pd = findPropertyDescriptor(entity, propertyName);
			if (pd != null) {
				Type _type = pd.getReadMethod().getGenericReturnType();
				if (_type instanceof Class) {
					clazz = (Class) _type;
				} else if (_type instanceof ParameterizedType) {
					ParameterizedType type = (ParameterizedType) _type;
					if (Collection.class.isAssignableFrom((Class) type.getRawType())) {
						clazz = (Class) type.getActualTypeArguments()[0];
					} else if (Map.class.isAssignableFrom((Class) type.getRawType())) {
						clazz = (Class) type.getActualTypeArguments()[1];
					}
				}
			}
		}
		if (clazz != null) {
			clazz = putIfAbsent(propertyActualTypeCaches, key, clazz);
		}
		return clazz;
	}

	public Class findPropertyRawType(Object entity, String propertyName) throws MappingException {
		checkEntity(entity);
		return findPropertyDescriptor(entity, propertyName).getPropertyType();
	}

	public <T extends Annotation> T findPropertyAnnotation(Object entity, String propertyName, Class<T> annotationClass) throws MappingException {
		checkEntity(entity);
		return findPropertyDescriptor(entity, propertyName).getReadMethod().getAnnotation(annotationClass);
	}

	public Object getProperty(Object entity, String propertyName) throws MappingException {
		try {
			if (entity instanceof Map) {
				return ((Map) entity).get(propertyName);
			} else {
				Method m = findPropertyDescriptor(entity, propertyName).getReadMethod();
				if (m == null) {
					throw new MappingException(String.format("[%s] no read method of property[%s]", entity.getClass(), propertyName));
				}
				return m.invoke(entity);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new MappingException(ex);
		}
	}

	public void setProperty(Object entity, String propertyName, Object value) throws MappingException {
		try {
			if (entity instanceof Map) {
				((Map) entity).put(propertyName, value);
			} else {
				Method m = findPropertyDescriptor(entity, propertyName).getWriteMethod();
				if (m == null) {
					throw new MappingException(String.format("[%s] no write method of property[%s]", entity.getClass(), propertyName));
				}
				m.invoke(entity, value);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new MappingException(ex);
		}
	}

	public Class findClass(String className) throws MappingException {
		try {
			switch (className) {
				case "list":
					return ArrayList.class;
				case "map":
					return HashMap.class;
				default:
					if (className.indexOf('.') < 0 && config.getDefaultEntityPackage() != null) {
						className = config.getDefaultEntityPackage() + "." + className;
					}
					return Class.forName(className);
			}
		} catch (ClassNotFoundException ex) {
			throw new MappingException(className, ex);
		}
	}

	public Object newInstance(String className) throws MappingException {
		try {
			return findClass(className).newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new MappingException(className, ex);
		}
	}

	public Object newInstance(Class type) throws MappingException {
		try {
			return type.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new MappingException(type.getName(), ex);
		}
	}

	public String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
	}

	public MappingConfig getConfig() {
		return config;
	}

	public void setConfig(MappingConfig config) {
		this.config = config;
	}
}
