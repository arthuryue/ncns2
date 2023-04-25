
/**
 * The persistent class for the T_JUDGM database table.
 *
 */
@Entity
@Table(name = "V_JUDGM")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NamedQuery(name = "Judgm.findAll", query = "SELECT t FROM Judgm t")
public class Judgm implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "JUDGM_ID")
    @GeneratedValue(generator = "SEQ_JUDGM")
    @SequenceGenerator(name = "SEQ_JUDGM", sequenceName = "SEQ_JUDGM", allocationSize = 1)
//    @SequenceGenerator(name="T_JUDGM_JUDGMID_GENERATOR" )
//    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="T_JUDGM_JUDGMID_GENERATOR")
    private long judgmId;

    @Column(name = "CREATE_BY")
    private String createBy;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    // @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "DOC_TYPE")
    private String docType;

    @Column(name = "HRNG_OR_PAPER")
    private String hrngOrPaper;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = "HAND_DOWN_DATE")
    private LocalDateTime handDownDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = "REVOKED_DATE")
    private LocalDateTime revokedDate;

    @Column(name = "REVOKED_BY")
    private String revokeBy;


    @Column(name = "NCN_CRT")
    private String ncnCrt;

    @Column(name = "NCN_NO")
    private Integer ncnNo;

    @Column(name = "NCN_YR")
    private Integer ncnYr;

    @Column(name = "RESTRICTED")
    private String restricted;

    @Column(name = "NCN_IN_LRS")
    private String ncnInLrs;
    
    @Column(name = "CONV_IND")
    private String convInd;

    private String status;

    @OneToMany(mappedBy = "Judgm", fetch = FetchType.LAZY)
    @OrderBy(value = "primary desc")
    private Set<JudgmCase> JudgmCases;


    @OneToMany(mappedBy = "Judgm", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Set<JudgmJjo> JudgmJjos;

    // bi-directional many-to-one association to JudgmXfr
    @OneToMany(mappedBy = "Judgm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<JudgmXfr> JudgmXfrs;

    // bi-directional many-to-one association to JudgmXfr
    @OneToMany(mappedBy = "Judgm", fetch = FetchType.LAZY)
    private Set<JudgmApl> JudgmApls;

    public Judgm() {
    }

    public JudgmCase addJudgmCase(JudgmCase JudgmCase) {
   	 getJudgmCases().add(JudgmCase);
   	 JudgmCase.setJudgm(this);

   	 return JudgmCase;
    }

    public JudgmXfr addJudgmXfr(JudgmXfr JudgmXfr) {
   	 getJudgmXfrs().add(JudgmXfr);
   	 JudgmXfr.setJudgm(this);

   	 return JudgmXfr;
    }


}
