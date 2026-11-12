-- A API passou a guardar o e-mail em minúsculas e sem espaços nas pontas (cadastro e login normalizam
-- a entrada). Contas antigas cadastradas em caixa mista deixariam de conseguir logar sem esta correção.
UPDATE usuarios SET email = lower(trim(email));
