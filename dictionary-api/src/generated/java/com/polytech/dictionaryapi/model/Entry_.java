package com.polytech.dictionaryapi.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Entry.class)
public abstract class Entry_ {

	public static volatile SingularAttribute<Entry, String> definition;
	public static volatile SingularAttribute<Entry, Long> id;
	public static volatile SingularAttribute<Entry, String> wordtype;
	public static volatile SingularAttribute<Entry, String> word;

	public static final String DEFINITION = "definition";
	public static final String ID = "id";
	public static final String WORDTYPE = "wordtype";
	public static final String WORD = "word";

}

