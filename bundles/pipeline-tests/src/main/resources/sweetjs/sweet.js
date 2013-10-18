macro def {
  rule { $name $params $body } => {
    function $name $params $body
  }
}
def add (a, b) {
  return a + b;
}
