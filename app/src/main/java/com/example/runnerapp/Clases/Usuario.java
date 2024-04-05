package com.example.runnerapp.Clases;

public class Usuario {

    private String uid;
    private String email;
    private String pais;
    private String password;
    private String nombres;
    private String edad;
    private String genero;
    private String imagenPerfilUri;

    public Usuario(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getPais() { return pais; }

    public void setPais(String pais) { this.pais = pais; }

    public String getImagenPerfilUri() {
        return imagenPerfilUri;
    }

    public void setImagenPerfilUri(String imagenPerfilUri) {
        this.imagenPerfilUri = imagenPerfilUri;
    }
}
