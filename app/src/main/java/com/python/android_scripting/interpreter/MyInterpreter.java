package com.python.android_scripting.interpreter;

import com.googlecode.android_scripting.interpreter.Interpreter;

import java.util.ArrayList;
import java.util.List;

public class MyInterpreter {

    private Interpreter interpreter;

    public MyInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public Interpreter getInterpreter() {
        return this.interpreter;
    }

    public List<String> getArguments() {
        return new ArrayList<>();
    }

}
