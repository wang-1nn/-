package org.example.backend.entity.pojo;




public enum QuestionType {
    CHOICE("选择题"), JUDGE("判断题"),
    FILL("填空题"), SHORT("简答题");
    private final String cn;
    QuestionType(String cn){ this.cn = cn; }
    public String cn(){ return cn; }
}