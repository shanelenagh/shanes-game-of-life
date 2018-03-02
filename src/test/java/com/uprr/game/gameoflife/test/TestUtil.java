package com.uprr.game.gameoflife.test;

import java.lang.reflect.Field;

public final class TestUtil {
	
	public static <T> T getPrivateField(Object object, String fieldName)
		throws NoSuchFieldException, IllegalAccessException
	{
	
		Field fieldRef = null;
		
		@SuppressWarnings("unchecked")
		Class objClass = object.getClass();
		while (fieldRef == null && objClass != null) {
			try {
				fieldRef = objClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException nsfe) {
				objClass = objClass.getSuperclass();
			}
		}
		
		if (fieldRef == null)
			throw new NoSuchFieldException(
				String.format("Couldn't find field [%s] on class %s or any of its superclasses",
						fieldName, object.getClass().getCanonicalName())
			);
			
		fieldRef.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		T fieldValue = (T) fieldRef.get(object);
	
		return fieldValue;
	}		
}
