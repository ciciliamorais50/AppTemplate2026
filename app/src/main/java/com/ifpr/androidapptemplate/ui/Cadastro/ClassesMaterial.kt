package com.ifpr.androidapptemplate.ui.cadastro

data class CampoMaterial(
    val chave: String,
    val label: String,
    val tipo: TipoCampo = TipoCampo.TEXTO,
    val obrigatorio: Boolean = false
)

enum class TipoCampo {
    TEXTO, NUMERO, LISTA
}

object ClassesMaterial {

    val classes: List<ClasseMaterial> = listOf(
        ClasseMaterial(
            id = "motor_eletrico",
            nome = "Motor Elétrico",
            icone = "⚙️",
            campos = listOf(
                CampoMaterial("descricao", "Descrição", TipoCampo.TEXTO, true),
                CampoMaterial("fabricante", "Fabricante", TipoCampo.TEXTO, true),
                CampoMaterial("potencia_cv", "Potência (CV)", TipoCampo.NUMERO),
                CampoMaterial("potencia_kw", "Potência (kW)", TipoCampo.NUMERO),
                CampoMaterial("tensao", "Tensão (V)", TipoCampo.TEXTO),
                CampoMaterial("corrente_nominal", "Corrente Nominal (A)", TipoCampo.NUMERO),
                CampoMaterial("rotacao", "Rotação (RPM)", TipoCampo.NUMERO),
                CampoMaterial("frame", "Frame/Carcaça", TipoCampo.TEXTO),
                CampoMaterial("grau_protecao", "Grau de Proteção (IP)", TipoCampo.TEXTO),
                CampoMaterial("fator_servico", "Fator de Serviço", TipoCampo.NUMERO),
                CampoMaterial("rendimento", "Rendimento (%)", TipoCampo.NUMERO),
                CampoMaterial("unidade_medida", "Unidade de Medida", TipoCampo.TEXTO),
                CampoMaterial("numero_serie", "Número de Série", TipoCampo.TEXTO),
                CampoMaterial("observacao", "Observação", TipoCampo.TEXTO)
            )
        ),
        ClasseMaterial(
            id = "rolamento",
            nome = "Rolamento",
            icone = "🔩",
            campos = listOf(
                CampoMaterial("descricao", "Descrição", TipoCampo.TEXTO, true),
                CampoMaterial("fabricante", "Fabricante", TipoCampo.TEXTO, true),
                CampoMaterial("codigo_fabricante", "Código do Fabricante", TipoCampo.TEXTO),
                CampoMaterial("tipo", "Tipo", TipoCampo.TEXTO),
                CampoMaterial("diametro_interno", "Diâmetro Interno (mm)", TipoCampo.NUMERO),
                CampoMaterial("diametro_externo", "Diâmetro Externo (mm)", TipoCampo.NUMERO),
                CampoMaterial("largura", "Largura (mm)", TipoCampo.NUMERO),
                CampoMaterial("capacidade_carga", "Capacidade de Carga (kN)", TipoCampo.NUMERO),
                CampoMaterial("velocidade_limite", "Velocidade Limite (RPM)", TipoCampo.NUMERO),
                CampoMaterial("lubrificacao", "Tipo de Lubrificação", TipoCampo.TEXTO),
                CampoMaterial("unidade_medida", "Unidade de Medida", TipoCampo.TEXTO),
                CampoMaterial("observacao", "Observação", TipoCampo.TEXTO)
            )
        ),
        ClasseMaterial(
            id = "valvula",
            nome = "Válvula",
            icone = "🔧",
            campos = listOf(
                CampoMaterial("descricao", "Descrição", TipoCampo.TEXTO, true),
                CampoMaterial("fabricante", "Fabricante", TipoCampo.TEXTO, true),
                CampoMaterial("tipo", "Tipo de Válvula", TipoCampo.TEXTO),
                CampoMaterial("diametro_nominal", "Diâmetro Nominal (DN)", TipoCampo.NUMERO),
                CampoMaterial("pressao_maxima", "Pressão Máxima (bar)", TipoCampo.NUMERO),
                CampoMaterial("temperatura_maxima", "Temperatura Máxima (°C)", TipoCampo.NUMERO),
                CampoMaterial("material_corpo", "Material do Corpo", TipoCampo.TEXTO),
                CampoMaterial("material_sede", "Material da Sede", TipoCampo.TEXTO),
                CampoMaterial("acionamento", "Tipo de Acionamento", TipoCampo.TEXTO),
                CampoMaterial("conexao", "Tipo de Conexão", TipoCampo.TEXTO),
                CampoMaterial("norma", "Norma/Classe", TipoCampo.TEXTO),
                CampoMaterial("unidade_medida", "Unidade de Medida", TipoCampo.TEXTO),
                CampoMaterial("numero_serie", "Número de Série", TipoCampo.TEXTO),
                CampoMaterial("observacao", "Observação", TipoCampo.TEXTO)
            )
        ),
        ClasseMaterial(
            id = "correia",
            nome = "Correia",
            icone = "🔗",
            campos = listOf(
                CampoMaterial("descricao", "Descrição", TipoCampo.TEXTO, true),
                CampoMaterial("fabricante", "Fabricante", TipoCampo.TEXTO, true),
                CampoMaterial("tipo", "Tipo de Correia", TipoCampo.TEXTO),
                CampoMaterial("perfil", "Perfil", TipoCampo.TEXTO),
                CampoMaterial("comprimento", "Comprimento (mm)", TipoCampo.NUMERO),
                CampoMaterial("largura", "Largura (mm)", TipoCampo.NUMERO),
                CampoMaterial("espessura", "Espessura (mm)", TipoCampo.NUMERO),
                CampoMaterial("material", "Material", TipoCampo.TEXTO),
                CampoMaterial("potencia_transmitida", "Potência Transmitida (kW)", TipoCampo.NUMERO),
                CampoMaterial("codigo_fabricante", "Código do Fabricante", TipoCampo.TEXTO),
                CampoMaterial("unidade_medida", "Unidade de Medida", TipoCampo.TEXTO),
                CampoMaterial("observacao", "Observação", TipoCampo.TEXTO)
            )
        ),
        ClasseMaterial(
            id = "redutor",
            nome = "Redutor",
            icone = "⚙️",
            campos = listOf(
                CampoMaterial("descricao", "Descrição", TipoCampo.TEXTO, true),
                CampoMaterial("fabricante", "Fabricante", TipoCampo.TEXTO, true),
                CampoMaterial("tipo", "Tipo de Redutor", TipoCampo.TEXTO),
                CampoMaterial("relacao_reducao", "Relação de Redução", TipoCampo.NUMERO),
                CampoMaterial("potencia_entrada", "Potência de Entrada (kW)", TipoCampo.NUMERO),
                CampoMaterial("torque_saida", "Torque de Saída (Nm)", TipoCampo.NUMERO),
                CampoMaterial("rotacao_entrada", "Rotação de Entrada (RPM)", TipoCampo.NUMERO),
                CampoMaterial("rotacao_saida", "Rotação de Saída (RPM)", TipoCampo.NUMERO),
                CampoMaterial("fator_servico", "Fator de Serviço", TipoCampo.NUMERO),
                CampoMaterial("lubrificante", "Tipo de Lubrificante", TipoCampo.TEXTO),
                CampoMaterial("volume_oleo", "Volume de Óleo (L)", TipoCampo.NUMERO),
                CampoMaterial("numero_serie", "Número de Série", TipoCampo.TEXTO),
                CampoMaterial("unidade_medida", "Unidade de Medida", TipoCampo.TEXTO),
                CampoMaterial("observacao", "Observação", TipoCampo.TEXTO)
            )
        )
    )

    fun porId(id: String): ClasseMaterial? = classes.find { it.id == id }
}

data class ClasseMaterial(
    val id: String,
    val nome: String,
    val icone: String,
    val campos: List<CampoMaterial>
)