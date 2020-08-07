package com.bing.lan.es;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lb on 2020/8/7.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    Integer id;
    Long number;
    LocalDateTime create_time;
    Double price;
    String name;
    String title;
}
