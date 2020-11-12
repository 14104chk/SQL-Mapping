package ssll.rsm;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ssll.rsm.fn.EntityFunction;
import ssll.rsm.fn.Function;
import ssll.rsm.fn.Function.Feature;
import ssll.rsm.fn.ListFunction;
import ssll.rsm.fn.WriteFunction;

import static ssll.core.LangUitls.getOrElse;

public class Label {

	private static final Pattern P_FUNCTION = Pattern.compile(":(\\w+)(?:\\x28([\\.\\w]*(?:,[\\.\\w]+)*)\\x29)?");
	public static final Pattern P_LABLE = Pattern.compile("(?:(\\.+)(\\w+))?((?:" + P_FUNCTION.pattern() + ")*)");
	private static final Pattern P_PROPERTY = Pattern.compile("(\\w+)(?:\\x28\\d+\\x29)?(?:\\x28([a-zA-Z0-9\\$\\.]+)\\x29)?(\\..*)?");
	//
	protected final MappingContext context;
	protected final int columnIndex;
	protected final String text;
	protected String propertyName;
	//
	protected List<Function> functions;
	protected List<Property> properties;
	//
	protected List<Label> child;

	public Label(MappingContext context, int columnIndex, String text, Matcher m) throws SQLException {
		this.context = context;
		this.columnIndex = columnIndex;
		this.text = text;
		//
		this.propertyName = getOrElse(m.group(2), "");
		//
		this.functions = new ArrayList<>();
		Matcher m2 = P_FUNCTION.matcher(m.group(3));
		while (m2.find()) {
			String name = m2.group(1);
			String[] args = Optional.ofNullable(m2.group(2)).flatMap(e -> Optional.of(e.split(","))).orElseGet(() -> new String[0]);
			this.functions.add(context.getConfig().buildFunction(context, name, args));
		}
		Collections.sort(this.functions);
	}

	public static void parse(MappingContext context, Deque<Label> parents, NavigableMap<Integer, String> columnMap) throws MappingException, SQLException {
		Label label = null;
		for (Map.Entry<Integer, String> column; (column = columnMap.pollFirstEntry()) != null;) {
			String labelText = column.getValue();
			Matcher m = P_LABLE.matcher(labelText);
			if (m.matches()) {
				int len = getOrElse(m.group(1), "").length();
				if (len <= parents.size()) {
					while (len < parents.size()) {
						parents.removeLast();
					}
					label = new Label(context, column.getKey(), labelText, m);
					if (parents.isEmpty()) {
						if (!label.propertyName.isEmpty()) {
							throw new MappingException(String.format("[%s] propertyName must be empty", labelText));
						}
					} else {
						if (label.propertyName.isEmpty()) {
							throw new MappingException(String.format("[%s] propertyName must not be empty", labelText));
						}
					}
					break;
				} else {
					throw new MappingException(String.format("[%s] z-index is error", labelText));
				}
			}
		}
		//
		if (label == null) {
			return;
		}
		//
		Map<String, Property> properties = new HashMap<>();
		for (Map.Entry<Integer, String> column; (column = columnMap.firstEntry()) != null;) {
			int columnIndex = column.getKey();
			String labelText = column.getValue();
			Matcher m = P_PROPERTY.matcher(labelText);
			if (m.matches()) {
				String name = m.group(1);
				if (Boolean.TRUE.equals(context.getConfig().getOptions().get("ColumnToLowerCase"))) {
					name = name.toLowerCase();
				}
				Property t = context.getConfig().getPropertyReaders().buildProperty(labelText, columnIndex, name, getOrElse(m.group(2), ""));
				properties.put(t.getName(), t);
				columnMap.pollFirstEntry();
			} else {
				break;
			}
		}
		label.properties = new ArrayList<>(properties.values());
		Collections.sort(label.properties);
		label.properties = Collections.unmodifiableList(label.properties);
		//
		if (!parents.isEmpty()) {
			Label last = parents.getLast();
			if (last.child == null) {
				last.child = new ArrayList<>();
			}
			last.child.add(label);
		}
		//
		parents.add(label);
	}

	public static Label parse(MappingContext context, ResultSet resultSet) throws MappingException, SQLException {
		Deque<Label> parents = new ArrayDeque<>();
		NavigableMap<Integer, String> columnMap = new TreeMap<>();
		ResultSetMetaData md = resultSet.getMetaData();
		for (int i = 1, n = md.getColumnCount(); i <= n; i++) {
			columnMap.put(i, md.getColumnLabel(i));
		}
		while (!columnMap.isEmpty()) {
			parse(context, parents, columnMap);
		}
		if (parents.isEmpty()) {
			return null;
		}
		Label root = parents.getFirst();
		root.unmodified();
		return root;
	}

	private void unmodified() {
		this.properties = Collections.unmodifiableList(this.properties);
		if (this.child != null) {
			for (Label t : this.child) {
				t.unmodified();
			}
		}
	}

	public Object mapping(MappingContext context, Object parentEntity) throws MappingException, SQLException {
		List<Function> functions = new ArrayList<>(this.functions);
		//
		if ("this".equals(this.propertyName)) {
			functions.add(new WriteFunction() {
				@Override
				public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
					chain.setEntity(parentEntity);
					super.execute(context, parentEntity, label, chain);
				}

				@Override
				public List<Feature> features() {
					return Arrays.asList(Function.Feature.SET_ENTITY, Function.Feature.BUILD_ENTITY);
				}

			});
		}
		//
		int mask = 0;
		for (Function fn : functions) {
			for (Feature f : fn.features()) {
				if ((f.getMask() & mask) == 0) {
					mask |= f.getMask();
				}
			}
		}
		//
		if ((mask & Feature.BUILD_ENTITY.getMask()) == 0) {
			Class propertyType;
			if (parentEntity == null) {
				propertyType = null;
			} else if (parentEntity instanceof Map) {
				propertyType = null;
			} else {
				propertyType = context.getConfig().getBeanUtils().findPropertyActualType(parentEntity, this.propertyName);
			}
			if (propertyType != null) {
				functions.add(new EntityFunction(propertyType));
			} else {
				functions.add(new EntityFunction(context, new String[0]));
//				functions.add(new EntityFunction(null) {
//					@Override
//					public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
//						if (chain.getEntity() instanceof VirtualObject) {
//							throw new MappingException(String.format("[%s] Can't create entity.", label.text));
//						}
//						chain.doNext();
//					}
//
//				});
			}
		}
		//
		if ((mask & Feature.SET_ENTITY.getMask()) == 0) {
			if (propertyName.isEmpty()) {
				functions.add(new ListFunction());
			} else if (parentEntity instanceof Map) {
				functions.add(context.getConfig().getDefaultSetEntityFunction());
			} else {
				Class propertyType = context.getConfig().getBeanUtils().findPropertyRawType(parentEntity, this.propertyName);
				if (propertyType == null) {
					throw new MappingException(String.format("[%s] no such property(%s)", this.text, this.propertyName));
				}
				if (List.class.isAssignableFrom(propertyType)) {
					functions.add(new ListFunction());
				} else {
					functions.add(context.getConfig().getDefaultSetEntityFunction());
				}
			}
		}
		Collections.sort(functions);
		int[] lastOrder = null;
		for (Function fn : functions) {
			if (lastOrder == null) {
				lastOrder = new int[]{fn.order()};
			} else if (lastOrder[0] == fn.order()) {
				throw new MappingException(String.format("[%s] Function'order is same", this.text));
			} else {
				lastOrder[0] = fn.order();
			}
		}
		//
		FunctionChain chain = new FunctionChain(context, parentEntity, this, new ArrayDeque<>(functions));
		chain.doNext();
		Object entity = chain.getEntity();
		if (!(entity instanceof VirtualObject) && child != null) {
			for (Label t : child) {
				t.mapping(context, entity);
			}
		}
		return entity;
	}

	public Object getSelfValue() throws SQLException {
		return context.getResultSet().getObject(columnIndex);
	}

	public String getSelfStringValue() throws SQLException {
		return context.getResultSet().getString(columnIndex);
	}

	@Override
	public String toString() {
		return this.text;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public List<Property> getProperties() {
		return properties;
	}

}
