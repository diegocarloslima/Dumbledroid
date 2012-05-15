package io.leocad.dumbledroid.data;

import io.leocad.dumbledroid.data.xml.Node;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Vector;

import android.util.Log;

public class XmlReflector {

	public static void reflectXmlObject(Object model, Node node) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		Class<?> modelClass = model.getClass();
		
		//Treat attributes like fields
		if (node.attributes != null) {
			for (String key : node.attributes.keySet()) {
				
				String value = node.attributes.get(key);
				
				Field field = modelClass.getField(key);
				Class<?> type = field.getType();
				
				if (type == String.class) {
					field.set(model, value);

				} else if (type == boolean.class || type == Boolean.class) {
					field.set(model, Boolean.valueOf(value));

				} else if (type == int.class || type == Integer.class) {
					field.set(model, Integer.valueOf(value));

				} else if (type == double.class || type == Double.class) {
					field.set(model, Double.valueOf(value));

				}
			}
		}

		for (int i = 0; i < node.subnodes.size(); i++) {

			Node subnode = node.subnodes.get(i);

			try {
				Field field = modelClass.getField(subnode.name);

				if (field.getType() == List.class) {
					processListField(model, field, subnode);

				} else {
					processSingleField(model, field, subnode);
				}

			} catch (NoSuchFieldException e) {
				Log.w(XmlReflector.class.getName(), "Can not locate field named " + subnode.name);

			} catch (IllegalArgumentException e) {
				Log.w(XmlReflector.class.getName(), "Can not put a String in the field named " + subnode.name);

			} catch (IllegalAccessException e) {
				Log.w(XmlReflector.class.getName(), "Can not access field named " + subnode.name);
			
			} catch (InstantiationException e) {
				Log.w(XmlReflector.class.getName(), "Can not create an instance of the type defined in the field named " + subnode.name);
			}
		}
	}

	private static void processSingleField(Object model, Field field, Node node) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SecurityException, NoSuchFieldException {

		Class<?> type = field.getType();

		field.set(model, getObject(node, type));
	}

	private static Object getObject(Node node, Class<?> type) throws InstantiationException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

		if (type == String.class) {
			return node.text;

		} else if (type == boolean.class || type == Boolean.class) {
			return Boolean.valueOf(node.text);

		} else if (type == int.class || type == Integer.class) {
			return Integer.valueOf(node.text);

		} else if (type == double.class || type == Double.class) {
			return Double.valueOf(node.text);

		} else {
			Object obj;
			try {
				obj = type.newInstance();
				
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
			
			reflectXmlObject(obj, node);
			
			return obj;
		}
	}

	private static void processListField(Object object, Field field, Node node) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SecurityException, NoSuchFieldException {

		ParameterizedType genericType = (ParameterizedType) field.getGenericType();
		Class<?> childrenType = (Class<?>) genericType.getActualTypeArguments()[0];

		field.set(object, getList(node, childrenType));
	}

	private static List<?> getList(Node node, Class<?> childrenType) throws InstantiationException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

		List<Object> list = new Vector<Object>(node.subnodes.size());

		for (int i = 0; i < node.subnodes.size(); i++) {

			Object child = null;
			Node subnode = node.subnodes.get(i);

			if (childrenType == List.class) {
				child = getList(subnode, childrenType);

			} else {
				child = getObject(subnode, childrenType);
			}

			list.add(child);
		}

		return list;
	}
}
