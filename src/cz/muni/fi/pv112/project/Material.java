package cz.muni.fi.pv112.project;

import org.joml.Vector3f;

public class Material {

    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private float shininess;

    public Material(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float shininess) {
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
    }

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    public Vector3f getSpecularColor() {
        return specularColor;
    }

    public float getShininess() {
        return shininess;
    }

}
