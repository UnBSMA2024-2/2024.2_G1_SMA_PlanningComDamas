package puzzle;

import java.io.Serializable;
import java.util.List;

public class TmpDTO implements Serializable {
  public List<List<Position>> captures;
  public List<Position> moves;

  public TmpDTO(List<List<Position>> captures, List<Position> moves) {
    this.captures = captures;
    this.moves = moves;
  }
}