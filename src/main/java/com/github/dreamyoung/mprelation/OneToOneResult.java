package com.github.dreamyoung.mprelation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.LazyLoader;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dreamyoung.mprelation.FieldCondition.FieldCollectionType;

public class OneToOneResult<T, E> {
	private List<T> list;
	private Field[] fields;
	private Collection<E> CollectionAll;
	private boolean lazy;
	private Class<?> fieldClass;
	private String fieldCode;
	private String refColumn;
	private BaseMapper<E> mapperE;
	private FieldCollectionType fieldCollectionType;
	private ArrayList<Serializable> columnPropertyValueList;

	private Map<String, String> columnPropertyMap;
	private Map<String, String> refColumnPropertyMap;

	private FieldCondition<T> fc;
	private Map<String, Boolean> isExeSqlMap;
	private Map<String, Collection<E>> collectionMap;

	public OneToOneResult(Field[] fields) {
		isExeSqlMap = new HashMap<String, Boolean>();
		collectionMap = new HashMap<String, Collection<E>>();
		for (int i = 0; i < fields.length; i++) {
			isExeSqlMap.put(fields[i].getName(), false);
			Collection<E> c = null;
			collectionMap.put(fields[i].getName(), c);
		}
	}

	public void handle(Field field) {
		List<E> listAll = null;
		if (!lazy) {
			listAll = (List<E>) CollectionAll;
		}

		if (listAll != null && listAll.size() > 0) {
			for (int j = 0; j < list.size(); j++) {
				T entity = list.get(j);
				String columnProperty = columnPropertyMap.get(fieldCode);
				String refColumnProperty = refColumnPropertyMap.get(fieldCode);

				E objForThisEntity = null;

				for (int k = 0; k < listAll.size(); k++) {
					E entityE = listAll.get(k);
					Field entityField = null;
					Field entity2Field = null;
					try {
						entityField = entity.getClass().getDeclaredField(columnProperty);
						entityField.setAccessible(true);
						Object columnValue = entityField.get(entity);

						entity2Field = entityE.getClass().getDeclaredField(refColumnProperty);
						entity2Field.setAccessible(true);
						Object refCoumnValue = entity2Field.get(entityE);

						if (columnValue.toString().equals(refCoumnValue.toString())) {
							objForThisEntity = entityE;
							break;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}

				}

				try {
					field.set(entity, objForThisEntity);
				} catch (

				Exception e) {
					e.printStackTrace();
				}
			} // end loop-entity
		} // end if

	}

	public void handleLazy(Field field) {
		final BaseMapper<E> mapper = (BaseMapper<E>) this.mapperE;

		ArrayList<Serializable> idListDistinct = new ArrayList<Serializable>();
		if (columnPropertyValueList.size() > 0) {
			for (int s = 0; s < columnPropertyValueList.size(); s++) {
				boolean isExists = false;
				for (int ss = 0; ss < idListDistinct.size(); ss++) {
					if (columnPropertyValueList.get(s).toString().equals(idListDistinct.get(ss).toString())) {
						isExists = true;
						break;
					}
				}

				if (!isExists) {
					idListDistinct.add(columnPropertyValueList.get(s));
				}
			}
		}
		columnPropertyValueList = idListDistinct;

		for (int i = 0; i < this.list.size(); i++) {
			T entity = list.get(i);

			@SuppressWarnings("unchecked")
			Class<E> entityEClass = (Class<E>) field.getType();

			@SuppressWarnings("unchecked")
			E objForThisEntityProxy = (E) Enhancer.create(entityEClass, new LazyLoader() {
				@Override
				public E loadObject() throws Exception {
					if (isExeSqlMap.get(field.getName()) == false) {
						collectionMap.put(field.getName(),
								mapper.selectList(new QueryWrapper<E>().in(refColumn, columnPropertyValueList)));
						isExeSqlMap.put(field.getName(), true);
					}

					List<E> listAll = (List<E>) collectionMap.get(field.getName());

					String columnProperty = columnPropertyMap.get(fieldCode);
					String refColumnProperty = refColumnPropertyMap.get(fieldCode);

					E objForThisEntity = null;

					for (int k = 0; k < listAll.size(); k++) {
						E entityE = listAll.get(k);
						Field entityField = null;
						Field entity2Field = null;
						try {
							entityField = entity.getClass().getDeclaredField(columnProperty);
							entityField.setAccessible(true);
							Object columnValue = entityField.get(entity);

							entity2Field = entityE.getClass().getDeclaredField(refColumnProperty);
							entity2Field.setAccessible(true);
							Object refCoumnValue = entity2Field.get(entityE);

							if (columnValue.toString().equals(refCoumnValue.toString())) {
								objForThisEntity = entityE;
								break;
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}

					}

					if (listAll == null || listAll.size() == 0 || objForThisEntity == null) {
						Class<E> e2Class = (Class<E>) fieldClass;
						objForThisEntity = e2Class.newInstance();
					}

					return (E) objForThisEntity;
				}

			});

			// 设置代理
			try {
				field.set(entity, objForThisEntityProxy);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static <E> List<E> getListResult(Field field) {
		return null;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public String getFieldCode() {
		return fieldCode;
	}

	public void setFieldCode(String fieldCode) {
		this.fieldCode = fieldCode;
	}

	public String getRefColumn() {
		return refColumn;
	}

	public void setRefColumn(String refColumn) {
		this.refColumn = refColumn;
	}

	public BaseMapper<E> getMapperE() {
		return mapperE;
	}

	public void setMapperE(BaseMapper<E> mapperE) {
		this.mapperE = mapperE;
	}

	public FieldCollectionType getFieldCollectionType() {
		return fieldCollectionType;
	}

	public void setFieldCollectionType(FieldCollectionType fieldCollectionType) {
		this.fieldCollectionType = fieldCollectionType;
	}

	public ArrayList<Serializable> getColumnPropertyValueList() {
		return columnPropertyValueList;
	}

	public void setColumnPropertyValueList(ArrayList<Serializable> columnPropertyValueList) {
		this.columnPropertyValueList = columnPropertyValueList;
	}

	public Map<String, String> getRefColumnPropertyMap() {
		return refColumnPropertyMap;
	}

	public void setRefColumnPropertyMap(Map<String, String> refColumnPropertyMap) {
		this.refColumnPropertyMap = refColumnPropertyMap;
	}

	public Map<String, String> getColumnPropertyMap() {
		return columnPropertyMap;
	}

	public void setColumnPropertyMap(Map<String, String> columnPropertyMap) {
		this.columnPropertyMap = columnPropertyMap;
	}

	public FieldCondition<T> getFc() {
		return fc;
	}

	public void setFc(FieldCondition<T> fc) {
		this.fc = fc;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public Collection<E> getCollectionAll() {
		return CollectionAll;
	}

	public void setCollectionAll(Collection<E> collectionAll) {
		CollectionAll = collectionAll;
	}

	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public Class<?> getFieldClass() {
		return fieldClass;
	}

	public void setFieldClass(Class<?> fieldClass) {
		this.fieldClass = fieldClass;
	}
}
