package com.example.demo.search.Helpers;

public class Pair<First,Second> {
    private First l;
    private Second r;
    public Pair(First l, Second r){
        this.l = l;
        this.r = r;
    }
    public First getFirst(){ return l; }
    public Second getSecond(){ return r; }
    public void setFirst(First l){ this.l = l; }
    public void setSecond(Second r){ this.r = r; }
}