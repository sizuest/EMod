%% FFT of input voltage

pwm.Umax = 400;
pwm.f    = 20e3;

input.U  = 330;
input.fs = 500;

fPWMSamples=[100,1000,10000];

col={'k','b','g','r','y'}

for i=1:length(fPWMSamples)
    
    pwm.f    = fPWMSamples(i);
    sim('PWMSignalAnalysis');

    % Values:
    time = ScpPWM.time;
    volt = ScpPWM.signals(1).values(:,1);

    fs = 20*pwm.f ;
    N = 2^nextpow2(length(volt));
    U = fft(volt)/length(volt);
    f = fs/2*linspace(0,1,N/2+1);

    h(i) = plot(f, 2*abs(U(1:N/2+1)),col{i}); hold on
end

