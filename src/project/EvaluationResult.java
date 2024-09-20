package project;

public class EvaluationResult {

    private Double sensitivity;
    private Double specificity;

    public EvaluationResult ( double specificity , double sensitivity ){
        this.sensitivity = sensitivity;
        this.specificity = specificity;
    }

    public double getSpecificity (){
        return specificity;
    }
    public double getSensitivity (){
        return sensitivity;
    }
}
