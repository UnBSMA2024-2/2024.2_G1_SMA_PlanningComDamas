package com.mvnJade.app.world;

import java.util.*;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

public class WorldGrid  extends TilePane {
  private MapTile[][] mapTile;

  WorldGrid(MapTile[][] mapTile) {
    this.mapTile = mapTile;
    init();
  }

  private void init() {
    setPrefColumns(3);
    setPrefRows(3);
    setTileAlignment(Pos.CENTER);
    List<MapTile> toAdd = new ArrayList<>();
    for (int i = 0; i < mapTile.length; i++) {
      for (int j = 0; j < mapTile[i].length; j++) {
        MapTile tile = this.mapTile[i][j];
        toAdd.add(tile);
      }
    }
    getChildren().clear(); 
    
    // grid.getStyleClass().add(GRID_5);
    getChildren().setAll(toAdd);
  }

  public void render (MapTile[][] mapTile) {
    this.mapTile = mapTile;
    List<MapTile> toAdd = new ArrayList<>();
    for (int i = 0; i < mapTile.length; i++) {
      for (int j = 0; j < mapTile[i].length; j++) {
        MapTile tile = this.mapTile[i][j];
        toAdd.add(tile);
      }
    }
    getChildren().clear(); 
    
    // grid.getStyleClass().add(GRID_5);
    getChildren().setAll(toAdd);
  }

}
