package com.example.a17280.whether.entity;

/**
 * Created by 17280 on 2019/5/8.
 *
        *      ┌─┐       ┌─┐
        *   ┌──┘ ┴───────┘ ┴──┐
        *   │                 │
        *   │       ───       │
        *   │  ─┬┘       └┬─  │
        *   │                 │
        *   │       ─┴─       │
        *   │                 │
        *   └───┐         ┌───┘
        *       │         │
        *       │         │
        *       │         │
        *       │         └──────────────┐
        *       │                        │
        *       │                        ├─┐
        *       │                        ┌─┘
        *       │                        │
        *       └─┐  ┐  ┌───────┬──┐  ┌──┘
        *         │ ─┤ ─┤       │ ─┤ ─┤
        *         └──┴──┘       └──┴──┘
        *                神兽保佑
        *               代码无BUG!

 */

public class City {
    private String id;
    private String name;
    private String path;

    public void setId(String id) {
        this.id = id;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }

}
