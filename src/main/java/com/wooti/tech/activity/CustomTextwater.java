package com.wooti.tech.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import lombok.Data;

@Data
public class CustomTextwater implements TextWatcher {

    EditText editText;

    public CustomTextwater(){

    }

    public CustomTextwater(EditText editText){
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
