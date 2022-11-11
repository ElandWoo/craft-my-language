public interface Token {
    /**
     * type of Token
     * 
     * @return
     */
    public TokenType getType();

    /**
     * get the text of token
     * 
     * @return
     */
    public String getText();
}
