package com.polytech.dictionaryapi.model;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "entries")
public class Entry implements Serializable {

    private static final long serialVersionUID = 4272632491407277957L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 1, max = 25)
    private String word;

    @Size(min = 1, max = 20)
    private String wordtype;

    @Column(columnDefinition = "text", nullable = false)
    private String definition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordtype() {
        return wordtype;
    }

    public void setWordtype(String wordtype) {
        this.wordtype = wordtype;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return word.equals(entry.word) &&
                wordtype.equals(entry.wordtype) &&
                definition.equals(entry.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, wordtype, definition);
    }
}
