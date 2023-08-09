package com.example.turtles;

public class Turtle {

    private String id;
    private String name;
    private String age;
    private String uri;
    private String sesso;
    private int campo;
    private int codiceMicrochip;

    private String registrationDate;

    public Turtle() {
    }

    public Turtle(int campo, String uri) {
        this.uri = uri;
        this.campo = campo;
        this.age = "";
        this.sesso = "";
        this.name = "";
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCodiceMicrochip() {
        return codiceMicrochip;
    }

    public void setCodiceMicrochip(int codiceMicrochip) {
        this.codiceMicrochip = codiceMicrochip;
    }
    public int getCampo() {
        return campo;
    }

    public void setCampo(int campo) {
        this.campo = campo;
    }
    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }


    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }
}
