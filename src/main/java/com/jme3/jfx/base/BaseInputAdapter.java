package com.jme3.jfx.base;

/**
 * Created by jan on 03.06.16.
 */
abstract class BaseInputAdapter implements InputAdapter {

    private Context context;

    public BaseInputAdapter(Context context){
        this.context = context;
    }

    protected Context getContext(){
        return context;
    }



}
